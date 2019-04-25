package no.bibsys.aws.utils.stacks;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.ListRoleTagsRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.identitymanagement.model.Tag;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.logs.model.LogGroup;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.VersionListing;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;
import no.bibsys.aws.roles.CreateStackRole;
import no.bibsys.aws.roles.DeleteRoleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackWiperImpl implements StackWiper {

    private static final String LOG_MESSAGE_TEMPLATE = "%s->%s";
    private static final String DESTROYED_STACK_LOG_MESSAGE = "Destroyed Stack status code:{}";
    private static final Logger logger = LoggerFactory.getLogger(StackWiperImpl.class);
    private static final int WAIT_UNTIL_CHECKING_AGAIN = 1000;
    private static final int MAX_TIMES_TO_CHECK = 100;
    private static final String ARN_BUCKET_NAME_DELIMITER = ":::";
    private final transient PipelineStackConfiguration pipelineStackConfiguration;
    private final transient AmazonCloudFormation cloudFormationClient;
    private final transient AmazonS3 s3Client;
    private final transient AWSLambda lambdaClient;
    private final transient AWSLogs logsClient;
    private final transient AmazonIdentityManagement amazonIdentityManagement;

    public StackWiperImpl(PipelineStackConfiguration pipelineStackConfiguration,
        AmazonCloudFormation acf,
        AmazonS3 s3Client,
        AWSLambda lambdaClient,
        AWSLogs logsClient,
        AmazonIdentityManagement amazonIdentityManagement
    ) {
        this.pipelineStackConfiguration = pipelineStackConfiguration;
        this.cloudFormationClient = acf;
        this.s3Client = s3Client;
        this.lambdaClient = lambdaClient;
        this.logsClient = logsClient;
        this.amazonIdentityManagement = amazonIdentityManagement;
    }

    @Override
    public void wipeStacks() {

        Map<Stage, Integer> resourceDestructionStatusCodes =
            Stage.listStages().stream()
                .map(stage -> new SimpleEntry<>(stage, invokeDestroyLambdaFunction(stage)))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        resourceDestructionStatusCodes.entrySet().stream()
            .map(entry -> String.format(LOG_MESSAGE_TEMPLATE, entry.getKey(), entry.getValue()))
            .forEach(message -> logger.debug(DESTROYED_STACK_LOG_MESSAGE, message));

        // Delete buckets first because they cannot be deleted automatically when we delete a Stack

        StackResources stackResources = new StackResources(pipelineStackConfiguration.getPipelineStackName(),
            cloudFormationClient);
        if (stackResources.stackExists()) {
            deleteBuckets();
            deleteStacks();
            deleteLogs();
        }
        // should be executed last to allow the deletion of the test and final stack.
        deleteCreateStackRole();
    }

    private void deleteCreateStackRole() {
        List<Role> rolesToDelete = rolesForDeletion();
        List<String> roleNames = rolesToDelete.stream().map(Role::getRoleName).collect(Collectors.toList());
        String roleNamesList = String.join(",", roleNames);
        logger.info("Deleting roles:{}", roleNamesList);
        DeleteRoleHelper deleteRoleHelper = new DeleteRoleHelper(amazonIdentityManagement);
        rolesToDelete.forEach(deleteRoleHelper::deleteRole);
    }

    protected List<Role> rolesForDeletion() {
        return amazonIdentityManagement
            .listRoles()
            .getRoles()
            .stream()
            .filter(this::roleHasCorrectTags)
            .collect(Collectors.toList());
    }

    protected boolean roleHasCorrectTags(Role role) {

        Tag projectIdTag = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());

        Tag branchTag = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());

        Tag roleTag = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_ROLE)
            .withValue(CreateStackRole.ROLE_TAG_FOR_CREATE_STACK_ROLE);

        HashSet<Tag> expectedTagSet = new HashSet<>();
        expectedTagSet.add(projectIdTag);
        expectedTagSet.add(branchTag);
        expectedTagSet.add(roleTag);
        List<Tag> tags = amazonIdentityManagement
            .listRoleTags(new ListRoleTagsRequest().withRoleName(role.getRoleName())).getTags();

        return tags.containsAll(expectedTagSet);
    }

    public void deleteBuckets() {

        StackResources stackResources =
            new StackResources(
                pipelineStackConfiguration.getPipelineStackName(),
                cloudFormationClient
            );
        List<String> bucketNames = stackResources
            .getResources(ResourceType.S3_BUCKET)
            .stream().map(StackResource::getPhysicalResourceId)
            .map(this::extractBucketName)
            .collect(Collectors.toList());

        bucketNames.forEach(this::emptyAndDeleteBucket);
    }

    public List<DeleteStackResult> deleteStacks() {

        String stack = pipelineStackConfiguration.getPipelineConfiguration().getTestServiceStack();
        final DeleteStackResult deleteTeckStackResult = cloudFormationClient
            .deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(cloudFormationClient, stack);

        stack = pipelineStackConfiguration.getPipelineConfiguration().getFinalServiceStack();
        final DeleteStackResult deleteFinalStackResult =
            cloudFormationClient.deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(cloudFormationClient, stack);

        stack = pipelineStackConfiguration.getPipelineStackName();

        final DeleteStackResult deletePipelineStackResult = cloudFormationClient
            .deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(cloudFormationClient, stack);

        return Arrays
            .asList(deleteTeckStackResult, deleteFinalStackResult, deletePipelineStackResult);
    }

    private Integer invokeDestroyLambdaFunction(Stage stage) {
        String destroyFunctionName = null;
        try {
            destroyFunctionName = pipelineStackConfiguration.getPipelineConfiguration()
                .getDestroyLambdaFunctionName();
            destroyFunctionName = String.join("-", destroyFunctionName, stage.toString());
            InvokeRequest request = new InvokeRequest();
            request.withInvocationType(InvocationType.RequestResponse)
                .withFunctionName(destroyFunctionName);
            InvokeResult invokeResult = lambdaClient.invoke(request);
            return invokeResult.getStatusCode();
        } catch (ResourceNotFoundException e) {
            logger.error(String.format("Function %s could not be found", destroyFunctionName));
        }
        return -1;
    }

    private void deleteLogs() {

        List<String> logGroups =
            logsClient.describeLogGroups().getLogGroups().stream()
                .map(LogGroup::getLogGroupName)
                .filter(name -> filterLogGroups(pipelineStackConfiguration, name))
                .collect(Collectors.toList());

        logGroups.stream().map(group -> new DeleteLogGroupRequest().withLogGroupName(group))
            .forEach(logsClient::deleteLogGroup);
    }

    private boolean filterLogGroups(PipelineStackConfiguration conf, String name) {
        return name.contains(conf.getProjectId()) && name.contains(conf.getNormalizedBranchName());
    }

    private void awaitDeleteStack(AmazonCloudFormation acf, String stackname) {
        int counter = 0;
        List<String> stackNames = acf.describeStacks().getStacks().stream()
            .map(Stack::getStackName)
            .collect(Collectors.toList());

        while (stackNames.contains(stackname) && counter < MAX_TIMES_TO_CHECK) {
            stackNames = acf.describeStacks().getStacks().stream()
                .map(Stack::getStackName)
                .collect(Collectors.toList());
            counter++;
            try {
                Thread.sleep(WAIT_UNTIL_CHECKING_AGAIN);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private String extractBucketName(String physicalId) {
        String[] array = physicalId.split(ARN_BUCKET_NAME_DELIMITER);
        return array[array.length - 1];
    }

    @Override
    public void emptyAndDeleteBucket(String bucketName) {
        emptyBucket(bucketName, s3Client);
        s3Client.deleteBucket(bucketName);
    }

    private void emptyBucket(String bucketName, AmazonS3 s3) {
        VersionListing versionListing = s3
            .listVersions(new ListVersionsRequest().withBucketName(bucketName));

        while (versionListing.isTruncated()) {
            deleteVersionBatch(bucketName, s3, versionListing);
            versionListing = s3.listNextBatchOfVersions(versionListing);
        }
        deleteVersionBatch(bucketName, s3, versionListing);

        ObjectListing objectListing = s3.listObjects(bucketName);
        while (objectListing.isTruncated()) {
            deleteObjectBatch(bucketName, s3, objectListing);
            objectListing = s3.listNextBatchOfObjects(objectListing);
        }

        deleteObjectBatch(bucketName, s3, objectListing);

        // Be sure we have nothing more to delete
        if (!versionListing.getVersionSummaries().isEmpty() || !objectListing.getObjectSummaries()
            .isEmpty()) {
            emptyBucket(bucketName, s3);
        }
    }

    private void deleteObjectBatch(String bucketName, AmazonS3 s3, ObjectListing objectListing) {
        objectListing.getObjectSummaries()
            .forEach(object -> s3.deleteObject(bucketName, object.getKey()));
    }

    private void deleteVersionBatch(String bucketName, AmazonS3 s3, VersionListing versionListing) {
        versionListing.getVersionSummaries()
            .forEach(
                version -> s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));
    }
}
