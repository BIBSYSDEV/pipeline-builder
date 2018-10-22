package no.bibsys.utils;

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
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.roles.RoleManager;

public class StackBuilder {


    private final transient IoUtils ioUtils=new IoUtils();

    private final transient StackWiper stackWiper;
    private final transient GithubReader githubReader;
    private final transient  String branch;


    public StackBuilder(StackWiper wiper,GithubReader githubReader){
        this.stackWiper=wiper;
        this.githubReader = githubReader;


        this.branch=githubReader.getBranch();
    }


    public void createStacks()
        throws IOException {

        PipelineStackConfiguration pipelineStackConfiguration = pipelineStackConfiguration();

        stackWiper.wipeStacks(pipelineStackConfiguration);

        createPipelineStack(pipelineStackConfiguration);
    }


    public PipelineStackConfiguration pipelineStackConfiguration()
        throws IOException {
        return new PipelineStackConfiguration( githubReader);

    }



    private void createPipelineStack(PipelineStackConfiguration pipelineStackConfiguration)
        throws IOException {

        RoleManager roleManager=new RoleManager(pipelineStackConfiguration.getPipelineConfiguration());
        roleManager.createRole();
        CreateStackRequest createStackRequest = createStackRequest(pipelineStackConfiguration);
        AmazonCloudFormation acf = AmazonCloudFormationClientBuilder.defaultClient();
        acf.createStack(createStackRequest);
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
}
