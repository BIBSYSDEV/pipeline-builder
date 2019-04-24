package no.bibsys.aws.utils.stacks;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.roles.CreateStackRole;
import no.bibsys.aws.roles.CreateStackRoleImpl;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.utils.github.GithubReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackBuilder {

    private static final Logger log = LoggerFactory.getLogger(StackBuilder.class);
    private static final String CLOUDFORMATION_TEMPLATE_PARAMETER_GITHUB_OWNER = "GithubOwner";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_REPO = "GithubRepo";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_AUTH = "GithubAuth";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_NAME = "PipelineName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_BUCKETNAME =
        "PipelineBucketname";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_ROLENAME =
        "PipelineRolename";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_CREATE_STACK_ROLE_ARN =
        "CreateStackRoleArn";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_CREATE_STACK_ROLE_NAME = "CreateStackRoleName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_SOURCE_STAGE_OUTPUT_ARTIFACT =
        "SourceStageOutputArtifact";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_ID = "ProjectId";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_BRANCH = "ProjectBranch";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_NORMALIZED_BRANCH_NAME = "NormalizedBranchName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_OUTPUT_ARTIFACT =
        "CodebuildOutputArtifact";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_PROJECTNAME = "CodebuildProjectname";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_EXECUTE_TESTS_PROJECTNAME =
        "ExecuteTestsProjectname";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_TEST_SERVICE_STACK_NAME =
        "PipelineTestServiceStackName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_FINAL_SERVICE_STACK_NAME =
        "PipelineFinalServiceStackName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_INIT_FUNCTION_NAME = "InitFunctionName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_DESTROY_FUNCTION_NAME = "DestroyFunctionName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_TEST_PHASE_NAME = "TestPhaseName";
    private static final String CLOUD_FORMATION_TEMPLATE_PARAMETER_FINAL_PHASE_NAME = "FinalPhaseName";
    private static final String STACK_DOES_NOT_EXIST_WARNING = "Stack does not exist";
    private static final String TEMPLATES_RESOURCE_DIRECTORY = "templates";
    private static final String PIPELINE_TEMPLATE = "pipelineTemplate.yaml";

    private final transient StackWiper stackWiper;

    private final transient PipelineStackConfiguration pipelineStackConfiguration;
    private final transient AmazonCloudFormation cloudFormationClient;
    private final transient AmazonIdentityManagement amazonIdentityManagement;
    private final transient GithubReader githubReader;

    public StackBuilder(
        StackWiper wiper,
        PipelineStackConfiguration pipelineStackConfiguration,
        AmazonCloudFormation cloudFormationClient,
        AmazonIdentityManagement amazonIdentityManagement,
        GithubReader githubReader
    ) {
        this.cloudFormationClient = cloudFormationClient;
        this.stackWiper = wiper;
        this.pipelineStackConfiguration = pipelineStackConfiguration;
        this.amazonIdentityManagement = amazonIdentityManagement;
        this.githubReader = githubReader;
    }

    public void createStacks() throws Exception {
        try {
            stackWiper.wipeStacks();
        } catch (AmazonCloudFormationException e) {
            log.warn(STACK_DOES_NOT_EXIST_WARNING);
        }
        createNewCreateStackRole(pipelineStackConfiguration, this.githubReader);
        createPipelineStack(pipelineStackConfiguration);
    }

    private void createNewCreateStackRole(PipelineStackConfiguration pipelineStackConfiguration,
        GithubReader githubReader) throws Exception {
        CreateStackRole createStackRole
            = new CreateStackRoleImpl(githubReader, pipelineStackConfiguration,
            amazonIdentityManagement);
        createStackRole.createRole();
    }

    private void createPipelineStack(PipelineStackConfiguration pipelineStackConfiguration)
        throws IOException {
        CreateStackRequest createStackRequest = createStackRequest(pipelineStackConfiguration);
        cloudFormationClient.createStack(createStackRequest);
    }

    private CreateStackRequest createStackRequest(
        PipelineStackConfiguration pipelineStackConfiguration)
        throws IOException {
        CreateStackRequest createStackRequest = new CreateStackRequest();
        setBasicStackRequestParameters(createStackRequest, pipelineStackConfiguration);
        setPipelineStackTemplate(createStackRequest);
        setTemplateParameters(createStackRequest, pipelineStackConfiguration);

        return createStackRequest;
    }

    private void setBasicStackRequestParameters(CreateStackRequest createStackRequest,
        PipelineStackConfiguration pipelineStackConfiguration) {
        createStackRequest.setStackName(pipelineStackConfiguration.getPipelineStackName());
        createStackRequest.withCapabilities(Capability.CAPABILITY_NAMED_IAM);
    }

    private void setTemplateParameters(CreateStackRequest createStackRequest,
        PipelineStackConfiguration pipelineStack)
        throws IOException {

        List<Parameter> parameters = new ArrayList<>();

        parameters.add(newParameter(
            CLOUDFORMATION_TEMPLATE_PARAMETER_GITHUB_OWNER,
            pipelineStack.getGithubConf().getOwner()));
        parameters.add(newParameter(
            CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_REPO,
            pipelineStack.getGithubConf().getRepository()));
        parameters.add(newParameter(
            CLOUD_FORMATION_TEMPLATE_PARAMETER_GITHUB_AUTH,
            pipelineStack.getGithubConf().getOauth()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_NAME,
            pipelineStack.getPipelineConfiguration().getPipelineName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_BUCKETNAME,
            pipelineStack.getBucketName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_ROLENAME,
            pipelineStack.getPipelineRoleName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_CREATE_STACK_ROLE_ARN,
            getCreateStackRoleArn()));
        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_CREATE_STACK_ROLE_NAME,
            getCreateStackRoleArn()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_SOURCE_STAGE_OUTPUT_ARTIFACT,
            pipelineStack.getPipelineConfiguration().getSourceOutputArtifactName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_ID,
            pipelineStack.getProjectId()));
        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_PROJECT_BRANCH,
            pipelineStack.getBranchName()));
        parameters
            .add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_NORMALIZED_BRANCH_NAME,
                pipelineStack.getNormalizedBranchName()));

        parameters.add(
            newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_OUTPUT_ARTIFACT,
                pipelineStack.getCodeBuildConfiguration().getOutputArtifact()));
        parameters.add(
            newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_CODEBUILD_PROJECTNAME,
                pipelineStack.getCodeBuildConfiguration().getBuildProjectName()));

        parameters.add(
            newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_EXECUTE_TESTS_PROJECTNAME,
                pipelineStack.getCodeBuildConfiguration().getExecuteTestsProjectName()));

        parameters.add(newParameter(
            CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_TEST_SERVICE_STACK_NAME,
            pipelineStack.getPipelineConfiguration().getTestServiceStack()));

        parameters.add(newParameter(
            CLOUD_FORMATION_TEMPLATE_PARAMETER_PIPELINE_FINAL_SERVICE_STACK_NAME,
            pipelineStack.getPipelineConfiguration().getFinalServiceStack()));

        parameters.add(
            newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_INIT_FUNCTION_NAME,
                pipelineStack.getPipelineConfiguration().getInitLambdaFunctionName()));
        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_DESTROY_FUNCTION_NAME,
            pipelineStack.getPipelineConfiguration().getDestroyLambdaFunctionName()));

        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_TEST_PHASE_NAME,
            Stage.TEST.toString()));
        parameters.add(newParameter(CLOUD_FORMATION_TEMPLATE_PARAMETER_FINAL_PHASE_NAME,
            Stage.FINAL.toString()));

        createStackRequest.setParameters(parameters);
    }

    private void setPipelineStackTemplate(CreateStackRequest createStackRequest)
        throws IOException {
        String templateBody = IoUtils
            .resourceAsString(Paths.get(TEMPLATES_RESOURCE_DIRECTORY, PIPELINE_TEMPLATE));
        createStackRequest.setTemplateBody(templateBody);
    }

    private Parameter newParameter(String key, String value) {
        return new Parameter().withParameterKey(key).withParameterValue(value);
    }

    public String getCreateStackRoleArn() throws NoSuchEntityException {
        GetRoleResult getRoleResult = amazonIdentityManagement
            .getRole(new GetRoleRequest().withRoleName(pipelineStackConfiguration.getCreateStackRoleName()));
        return getRoleResult.getRole().getArn();
    }
}
