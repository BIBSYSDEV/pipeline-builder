package no.bibsys;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.utils.Environment;
import no.bibsys.utils.IoUtils;
import no.bibsys.utils.StackWiper;

public class Application {

    private final transient IoUtils ioUtils = new IoUtils();
    private final transient Environment environment;
    private final transient StackWiper wiper;


    private transient String repoName;
    private transient String branch;
    private transient String repoOwner;


    public Application(Environment environment) {
        this.environment = environment;
        wiper = new StackWiper();

    }


    public void createStacks()
        throws IOException {
        checkNulls();
        PipelineStackConfiguration pipelineStackConfiguration = pipelineStackConfiguration();
        wiper.wipeStacks(pipelineStackConfiguration);
        createPipelineStack(pipelineStackConfiguration);
    }


    public void wipeStacks() throws IOException {
        checkNulls();
        PipelineStackConfiguration conf = new PipelineStackConfiguration(
            branch,
            repoName,
            repoOwner,
            environment);
        wiper.wipeStacks(conf);

    }


    public PipelineStackConfiguration pipelineStackConfiguration()
        throws IOException {
        checkNulls();
        return new PipelineStackConfiguration(branch, repoName, repoOwner, environment);

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

        parameters.add(newParameter("CreateStackRolename", pipelineStack.getCreateStackRoleName()));

        parameters.add(newParameter("SourceStageOutputArtifact", pipelineStack
            .getPipelineConfiguration().getSourceOutputArtifactName()));

        parameters.add(newParameter("ProjectId", pipelineStack.getProjectId()));
        parameters.add(newParameter("ProjectBranch", pipelineStack.getBranchName()));
        parameters.add(newParameter("NormalizedBranchName", pipelineStack.getNormalizedBranchName()));

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


    private void createPipelineStack(PipelineStackConfiguration pipelineStackConfiguration)
        throws IOException {
        CreateStackRequest createStackRequest = createStackRequest(pipelineStackConfiguration);
        AmazonCloudFormation acf = AmazonCloudFormationClientBuilder.defaultClient();
        acf.createStack(createStackRequest);
    }

    private void checkNulls() {

        if (repoName == null) {
            throw new IllegalArgumentException("repoName is null");
        }
        if (branch == null) {
            throw new IllegalArgumentException("branch is null");
        }
        if (repoOwner == null) {
            throw new IllegalArgumentException("repoOwner is null");
        }

    }




    public Application withRepoName(String repository) {
        this.repoName = repository;


        return this;
    }

    public Application withBranch(String branch) {
        this.branch = branch;
        return this;
    }

    public Application withRepoOwner(String repoOwner) {
        this.repoOwner = repoOwner;
        return this;
    }




}
