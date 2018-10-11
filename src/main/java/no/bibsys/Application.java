package no.bibsys;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.VersionListing;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.utils.IOUtils;

public class Application {

    private IOUtils ioUtils = new IOUtils();


    public void run(String projectName, String branch, String repoName, String repoOwner,boolean initAuth)
        throws IOException {
        PipelineStackConfiguration pipelineStackConfiguration = pipelineStackConfiguration(
            projectName, branch, repoName, repoOwner,initAuth);
        deleteStacks(pipelineStackConfiguration);
        CreateStackRequest createStackRequest = createStackRequest(pipelineStackConfiguration);
        AmazonCloudFormation acf = AmazonCloudFormationClientBuilder.defaultClient();
        acf.createStack(createStackRequest);
    }


    public PipelineStackConfiguration pipelineStackConfiguration
        (String projectName, String branch, String repoName, String repoOwner, boolean initAuth)
        throws IOException {
        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(
            projectName, branch, repoName, repoOwner);
        if(initAuth){
            pipelineStackConfiguration.getGithubConf().setOAuth();
        }

        return pipelineStackConfiguration;

    }


    private CreateStackRequest createStackRequest(
        PipelineStackConfiguration pipelineStack) throws IOException {
        CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.setStackName(pipelineStack.getPipelineStackName());
        List<Parameter> parameters = new ArrayList<>();

        parameters.add(
            newParameter("GithubOwner", pipelineStack.getGithubConf().getOwner()));
        parameters
            .add(newParameter("GithubRepo", pipelineStack.getGithubConf().getRepo()));
        parameters.add(newParameter("GithubAuth", pipelineStack.getGithubConf().getOauth()));

        parameters.add(newParameter("PipelineName",
            pipelineStack.getPipelineConfiguration().getPipelineName()));

        parameters.add(newParameter("PipelineBucketname", pipelineStack.getBucketName()));

        parameters.add(newParameter("PipelineRolename", pipelineStack.getPipelineRoleName()));
        parameters.add(newParameter("PipelineLambdaTrustRoleName",
            pipelineStack.getPipelineConfiguration().getLambdaTrustRolename()));
        parameters.add(newParameter("ProjectStage", pipelineStack.getStage()));

        parameters.add(newParameter("CreateStackRolename", pipelineStack.getCreateStackRoleName()));

        parameters.add(newParameter("SourceStageOutputArtifact", pipelineStack
            .getPipelineConfiguration().getSourceOutputArtifactName()));

        parameters.add(newParameter("ProjectId", pipelineStack.getProjectId()));
        parameters.add(newParameter("ProjectBranch", pipelineStack.getBranchName()));

        parameters.add(newParameter("CodebuildOutputArtifact",
            pipelineStack.getCodeBuildConfiguration().getOutputArtifact()));
        parameters.add(newParameter("CodebuildProjectname",
            pipelineStack.getCodeBuildConfiguration().getBuildProjectName()));
        parameters.add(newParameter("CodebuildCache",
            pipelineStack.getCodeBuildConfiguration().getCacheFolder()));

        parameters.add(newParameter("ServiceStackName",
            pipelineStack.getPipelineConfiguration().getServiceStack()));

        createStackRequest.setParameters(parameters);
        createStackRequest.withCapabilities(Capability.CAPABILITY_NAMED_IAM);

        String templateBody = ioUtils
            .resourceAsString(Paths.get("templates", "pipelineTemplate.yaml"));
        createStackRequest.setTemplateBody(templateBody);

        return createStackRequest;

    }


    private Parameter newParameter(String key, String value) {
        return new Parameter().withParameterKey(key).withParameterValue(value);
    }

    public void deleteStacks(PipelineStackConfiguration pipelineStackConfiguration) {
        AmazonCloudFormation acf = AmazonCloudFormationClientBuilder.defaultClient();

        String systemStack = pipelineStackConfiguration.getPipelineConfiguration()
            .getServiceStack();

        DeleteStackRequest deleteStackRequest = new DeleteStackRequest()
            .withStackName(systemStack);

        acf.deleteStack(deleteStackRequest);
        awaitDeleteStack(acf, systemStack);

        String pipelineGenerationStack = pipelineStackConfiguration.getPipelineStackName();
        deleteBucket(pipelineStackConfiguration.getBucketName());
        deleteStackRequest = new DeleteStackRequest().withStackName(pipelineGenerationStack);
        acf.deleteStack(deleteStackRequest);

    }


    private void awaitDeleteStack(AmazonCloudFormation acf, String stackname) {

        int counter = 0;
        List<String> stackNames = acf.describeStacks().getStacks().stream()
            .map(stack -> stack.getStackName())
            .collect(Collectors.toList());

        while (stackNames.contains(stackname) && counter < 100) {
            stackNames = acf.describeStacks().getStacks().stream()
                .map(stack -> stack.getStackName())
                .collect(Collectors.toList());
            counter++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }


        }

    }


    private void deleteBucket(String bucketName) {
        try {
            AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
            emptyBucket(bucketName, s3);

            s3.deleteBucket(bucketName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void emptyBucket(String bucketName, AmazonS3 s3) {
        VersionListing versionListing = s3
            .listVersions(new ListVersionsRequest().withBucketName(bucketName));
        while (versionListing.isTruncated()) {
            versionListing.getVersionSummaries().forEach(version ->
                s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));
            versionListing = s3.listNextBatchOfVersions(versionListing);
        }
        versionListing.getVersionSummaries().forEach(version ->
            s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));

        ObjectListing objectListing = s3.listObjects(bucketName);
        while (objectListing.isTruncated()) {
            objectListing.getObjectSummaries().stream()
                .forEach(object -> s3.deleteObject(bucketName, object.getKey()));
            objectListing = s3.listNextBatchOfObjects(objectListing);
        }

        objectListing.getObjectSummaries().stream()
            .forEach(object -> s3.deleteObject(bucketName, object.getKey()));

        if (versionListing.isTruncated() || objectListing.isTruncated()) {
            emptyBucket(bucketName, s3);
        }

    }


}
