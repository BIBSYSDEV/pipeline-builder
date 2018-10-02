package no.bibsys;

import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import no.bibsys.cloudformation.PipelineStackConfiguration;

public class Application {


    private IOUtils ioUtils = new IOUtils();

    public static void main(String[] args) {
        Application application = new Application();
        application.pipeineStackConfiguration();
    }


    public PipelineStackConfiguration pipeineStackConfiguration() {
        String projectName = "emne-test";
        String branchName = "master";
        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(projectName, branchName);
        return pipelineStackConfiguration;

    }


    public CreateStackRequest createStackRequest(
        PipelineStackConfiguration pipelineStack) throws IOException {
        CreateStackRequest createStackRequest = new CreateStackRequest();
        createStackRequest.setStackName(pipelineStack.getName());
        List<Parameter> parameters = new ArrayList<>();
        parameters
            .add(newParameter("GithubAuth", pipelineStack.getGithubConf().getAuth()));
        parameters.add(
            newParameter("GithubOwner", pipelineStack.getGithubConf().getOwner()));
        parameters
            .add(newParameter("GithubRepo", pipelineStack.getGithubConf().getRepo()));
        parameters.add(newParameter("PipelineBucketname",
            pipelineStack.getPipelineConfiguration().getBucketName()));
        parameters.add(newParameter("PipelineRolename",
            pipelineStack.getPipelineConfiguration().getRoleName()));

        parameters.add(newParameter("SourceStageOutputArtifact",pipelineStack
            .getPipelineConfiguration().getSourceOutputArtifactName()));

        parameters.add(newParameter("ProjectId", pipelineStack.getProjectId()));
        parameters.add(newParameter("ProjectBranch", pipelineStack.getBranchName()));

        parameters.add(newParameter("CodebuildOutputArtifact",pipelineStack.getCodeBuildConfiguration().getOutputArtifact()));
        parameters.add(newParameter("CodebuildProjectname",pipelineStack.getCodeBuildConfiguration().getProjectName()));


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
