package no.bibsys;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
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
import no.bibsys.utils.Environment;
import no.bibsys.utils.IOUtils;

public class Application {

    private IOUtils ioUtils = new IOUtils();
     private String projectName;
     private String repoName;
     private String branch;
     private String repoOwner;
     private  Environment environment;


     public Application(Environment environment){
         this.environment=environment;
     }


    public void createStacks()
        throws IOException {

         checkNulls();
        PipelineStackConfiguration pipelineStackConfiguration = pipelineStackConfiguration(
            projectName, branch, repoName, repoOwner);
        wipeStacks(pipelineStackConfiguration);
        createPipelineStack(pipelineStackConfiguration);
    }


    public void wipeStacks() throws IOException {
        checkNulls();
        PipelineStackConfiguration conf=new PipelineStackConfiguration(projectName,
            branch,
            repoName,
            repoOwner,
            environment);
        wipeStacks(conf);

    }

    private void wipeStacks(PipelineStackConfiguration pipelineStackConfiguration) {
        checkNulls();
        deleteBuckets(pipelineStackConfiguration);
        deleteStacks(pipelineStackConfiguration);
        deleteLogs(pipelineStackConfiguration);
    }


    public PipelineStackConfiguration pipelineStackConfiguration
        (String projectName, String branch, String repoName, String repoOwner)
        throws IOException {
        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(
            projectName, branch, repoName, repoOwner,environment);

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
            pipelineStack.getCodeBuildConfiguration().getCacheBucket()));

        parameters.add(newParameter("PipelineTestServiceStackName",
            pipelineStack.getPipelineConfiguration().getTestServiceStack()));

        parameters.add(newParameter("PipelineFinalServiceStackName",
            pipelineStack.getPipelineConfiguration().getFinalServiceStack()));

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

    public void deleteStacks(PipelineStackConfiguration conf) {
        AmazonCloudFormation acf = AmazonCloudFormationClientBuilder.defaultClient();

        String stack = conf.getPipelineConfiguration()
            .getTestServiceStack();

        acf.deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(acf, stack);

        stack =conf.getPipelineConfiguration().getFinalServiceStack();
        acf.deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(acf, stack);

        stack = conf.getPipelineStackName();

        acf.deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(acf, stack);

    }

    private void deleteBuckets(PipelineStackConfiguration pipelineStackConfiguration) {
        deleteBucket(pipelineStackConfiguration.getBucketName());
        deleteBucket(pipelineStackConfiguration.getCodeBuildConfiguration().getCacheBucket());
    }


    private void deleteLogs(PipelineStackConfiguration conf) {
        AWSLogs logsClient = AWSLogsClientBuilder.defaultClient();
        List<String> logGroups = logsClient
            .describeLogGroups().getLogGroups().stream()
            .map(group -> group.getLogGroupName())
            .filter(name -> filterLogGroups(conf, name))
            .collect(Collectors.toList());

        logGroups.stream()
            .map(group -> new DeleteLogGroupRequest().withLogGroupName(group))
            .forEach(request -> logsClient.deleteLogGroup(request));


    }

    private boolean filterLogGroups(PipelineStackConfiguration conf, String name) {
        boolean result = name.contains(conf.getProjectId()) &&
            name.contains(conf.getShortBranch());
        return result;
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
        if(!versionListing.getVersionSummaries().isEmpty()||
         !objectListing.getObjectSummaries().isEmpty()){
               emptyBucket(bucketName,s3);
        }

    }

    private void deleteObjectBatch(String bucketName, AmazonS3 s3, ObjectListing objectListing) {
        objectListing.getObjectSummaries().stream()
            .forEach(object -> s3.deleteObject(bucketName, object.getKey()));
    }

    private void deleteVersionBatch(String bucketName, AmazonS3 s3, VersionListing versionListing) {
        versionListing.getVersionSummaries().forEach(version ->
            s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));
    }

    private void createPipelineStack(PipelineStackConfiguration pipelineStackConfiguration)
        throws IOException {
        CreateStackRequest createStackRequest = createStackRequest(pipelineStackConfiguration);
        AmazonCloudFormation acf = AmazonCloudFormationClientBuilder.defaultClient();
        acf.createStack(createStackRequest);
    }

    private void checkNulls(){
        if(projectName==null){
            throw new NullPointerException("projectName is null");
        }
        if(repoName==null){
            throw  new NullPointerException("repoName is null");
        }
        if(branch==null){
            throw  new NullPointerException("branch is null");
        }
        if(repoOwner==null){
            throw  new NullPointerException("repoOwner is null");
        }

    }


    public Application withProjectName(String projectName){
        this.projectName=projectName;
        return this;
    }

    public Application withRepoName(String repository){
        this.repoName=repository;
        return this;
    }

    public Application withBranch(String branch){
        this.branch=branch;
        return this;
    }

    public Application withRepoOwner(String repoOwner){
        this.repoOwner=repoOwner;
        return this;
    }




}
