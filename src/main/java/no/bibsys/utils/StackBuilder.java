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
import no.bibsys.cloudformation.Stage;

public class StackBuilder {

    private final transient StackWiper stackWiper;


    private final transient PipelineStackConfiguration pipelineStackConfiguration;


    public StackBuilder(StackWiper wiper, PipelineStackConfiguration pipelineStackConfiguration) {
        this.stackWiper = wiper;
        this.pipelineStackConfiguration=pipelineStackConfiguration;


    }


    public void createStacks()
        throws IOException {
        stackWiper.wipeStacks();
        createPipelineStack(pipelineStackConfiguration);
    }





    private void createPipelineStack(PipelineStackConfiguration pipelineStackConfiguration)
        throws IOException {
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

        parameters.add(newParameter("CreateStackRolename", pipelineStack.getCreateStackRoleName()));

        parameters.add(newParameter("SourceStageOutputArtifact", pipelineStack
            .getPipelineConfiguration().getSourceOutputArtifactName()));

        parameters.add(newParameter("ProjectId", pipelineStack.getProjectId()));
        parameters.add(newParameter("ProjectBranch", pipelineStack.getBranchName()));
        parameters
            .add(newParameter("NormalizedBranchName", pipelineStack.getNormalizedBranchName()));

        parameters.add(newParameter("CodebuildOutputArtifact",
            pipelineStack.getCodeBuildConfiguration().getOutputArtifact()));
        parameters.add(newParameter("CodebuildProjectname",
            pipelineStack.getCodeBuildConfiguration().getBuildProjectName()));


        parameters.add(newParameter("PipelineTestServiceStackName",
            pipelineStack.getPipelineConfiguration().getTestServiceStack()));

        parameters.add(newParameter("PipelineFinalServiceStackName",
            pipelineStack.getPipelineConfiguration().getFinalServiceStack()));

        parameters.add(newParameter("InitFunctionName",pipelineStack.getPipelineConfiguration().getInitLambdaFunctionName()));
        parameters.add(newParameter("DestroyFunctionName",pipelineStack.getPipelineConfiguration().getDestroyLambdaFunctionName()));

        parameters.add(newParameter("TestPhaseName", Stage.TEST));
        parameters.add(newParameter("FinalPhaseName", Stage.FINAL));

        createStackRequest.setParameters(parameters);
        createStackRequest.withCapabilities(Capability.CAPABILITY_NAMED_IAM);

        String templateBody = IoUtils
            .resourceAsString(Paths.get("templates", "pipelineTemplate.yaml"));
        createStackRequest.setTemplateBody(templateBody);

        return createStackRequest;

    }


    private Parameter newParameter(String key, String value) {
        return new Parameter().withParameterKey(key).withParameterValue(value);
    }
}
