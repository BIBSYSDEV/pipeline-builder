package no.bibsys.aws.utils.stacks;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackWiper {

    private static final Logger logger = LoggerFactory.getLogger(StackWiper.class);
    private static final int WAIT_UNTIL_CHECKING_AGAIN = 1000;
    private static final int MAX_TIMES_TO_CHECK = 100;

    private final transient PipelineStackConfiguration pipelineStackConfiguration;
    private final transient AmazonCloudFormation cloudFormationClient;
    private final transient AmazonS3 s3Client;
    private final transient AWSLambda lambdaClient;
    private final transient AWSLogs logsClient;

    public StackWiper(PipelineStackConfiguration pipelineStackConfiguration,
        AmazonCloudFormation acf,
        AmazonS3 s3Client,
        AWSLambda lambdaClient,
        AWSLogs logsClient
    ) {
        this.pipelineStackConfiguration = pipelineStackConfiguration;
        this.cloudFormationClient = acf;
        this.s3Client = s3Client;
        this.lambdaClient = lambdaClient;
        this.logsClient = logsClient;
    }

    public void wipeStacks() {

        Map<Stage, Integer> statusCodes =
            Stage.listStages().stream()
                .map(stage -> new SimpleEntry<>(stage, invokeDestroyLambdaFunction(stage)))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

        statusCodes.entrySet().stream()
            .map(entry -> String.format("%s->%s", entry.getKey(), entry.getValue()))
            .forEach(message -> logger.debug("Destroyed Stack status code:", message));

        // Delete buckets first because they cannot be deleted automatically when we delete a Stack
        deleteBuckets();
        deleteStacks();
        deleteLogs();
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

        bucketNames.forEach(this::deleteBucket);
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
        String[] array = physicalId.split(":::");
        return array[array.length - 1];
    }

    public void deleteBucket(String bucketName) {
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
