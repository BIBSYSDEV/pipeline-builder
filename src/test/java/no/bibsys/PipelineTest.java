package no.bibsys;

import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codebuild.AWSCodeBuildClientBuilder;
import com.amazonaws.services.codebuild.model.ArtifactsType;
import com.amazonaws.services.codebuild.model.ComputeType;
import com.amazonaws.services.codebuild.model.CreateProjectRequest;
import com.amazonaws.services.codebuild.model.DeleteProjectRequest;
import com.amazonaws.services.codebuild.model.EnvironmentType;
import com.amazonaws.services.codebuild.model.ProjectArtifacts;
import com.amazonaws.services.codebuild.model.ProjectEnvironment;
import com.amazonaws.services.codebuild.model.ProjectSource;
import com.amazonaws.services.codebuild.model.ResourceAlreadyExistsException;
import com.amazonaws.services.codebuild.model.SourceType;
import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionDeclaration;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.ActionTypeId;
import com.amazonaws.services.codepipeline.model.ArtifactStore;
import com.amazonaws.services.codepipeline.model.CreatePipelineRequest;
import com.amazonaws.services.codepipeline.model.DeletePipelineRequest;
import com.amazonaws.services.codepipeline.model.InputArtifact;
import com.amazonaws.services.codepipeline.model.OutputArtifact;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.DetachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.Test;

public class PipelineTest implements EnvUtils {


    private final String buildProjectName = "JavaPPBuild";
    private final String s3Bucket = "java-pipeline";
    private final String pipelineName = "testJavaPipeline";
    private final String s3BucketAccessInlinePolicyName = String
        .join("_", "Custom", "S3", pipelineName, s3Bucket);
    IOUtils ioUtils = new IOUtils();

    @Test
    public void testPipeline() throws IOException, InterruptedException {
        Role role = createRole();
        createBuildProject(role, 0);
        AWSCodePipeline client = AWSCodePipelineClientBuilder.defaultClient();
        try{
            client.deletePipeline(new DeletePipelineRequest().withName(pipelineName));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        CreatePipelineRequest request = new CreatePipelineRequest();

        PipelineDeclaration pipelineDeclaration = new PipelineDeclaration();
        pipelineDeclaration.setName(pipelineName);
        pipelineDeclaration.setRoleArn(role.getArn());
        pipelineDeclaration.setArtifactStore(new ArtifactStore()
            .withType("S3").withLocation(s3Bucket));

        List<StageDeclaration> stageDeclarations = new ArrayList<>();
        addSourceStage(stageDeclarations, role);
        addBuildStage(stageDeclarations);
        pipelineDeclaration.setStages(stageDeclarations);
        request.setPipeline(pipelineDeclaration);
        client.createPipeline(request);

    }





    private void deleteExistingRole(GetRoleRequest getRole) {
        Optional<GetRoleResult> getRoleResponse = getRole(getRole);
        getRoleResponse.ifPresent(getRoleResult -> deleteRole(getRoleResult.getRole()));
    }

    private PutRolePolicyRequest bucketAccessInlinePolicy(String roleName) throws IOException {

        String accessToBucket = ioUtils
            .resourceAsString(Paths.get("policies", "accessToBucket.json"));

        return new PutRolePolicyRequest()
            .withPolicyDocument(accessToBucket)
            .withRoleName(roleName)
            .withPolicyName(s3BucketAccessInlinePolicyName);
    }

    private List<String> listDefaultAmazonPolicies() {
        List<String> policies = new ArrayList<>();
        policies.add("arn:aws:iam::aws:policy/AWSCodePipelineFullAccess");
        policies.add("arn:aws:iam::aws:policy/AWSCodeCommitFullAccess");
        policies.add("arn:aws:iam::aws:policy/AWSCodeBuildAdminAccess");
//        policies.add("arn:aws:iam::aws:policy/AmazonS3FullAccess");
        return policies;
    }


    private void deleteRole(Role role) {
        final String roleName = role.getRoleName();
        ListAttachedRolePoliciesRequest request = new ListAttachedRolePoliciesRequest();
        request.withRoleName(role.getRoleName());
        AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.defaultClient();
        ListAttachedRolePoliciesResult result = iam
            .listAttachedRolePolicies(request);

        Stream<DetachRolePolicyRequest> detatchRequests = result.getAttachedPolicies().stream()
            .map(policy -> new DetachRolePolicyRequest()
                .withPolicyArn(policy.getPolicyArn())
                .withRoleName(roleName)
            );

        detatchRequests.forEach(iam::detachRolePolicy);

        iam.deleteRolePolicy(new DeleteRolePolicyRequest()
            .withPolicyName(s3BucketAccessInlinePolicyName).withRoleName(roleName));
        iam.deleteRole(new DeleteRoleRequest().withRoleName(roleName));


    }

    private void attacheRolePolicy(AmazonIdentityManagement iam, String roleName,
        String policyArn) {
        AttachRolePolicyRequest attachRoleRequest = new AttachRolePolicyRequest();
        attachRoleRequest.setPolicyArn(policyArn);

        attachRoleRequest.setRoleName(roleName);
        iam.attachRolePolicy(attachRoleRequest);
    }


    private Optional<GetRoleResult> getRole(GetRoleRequest getRole) {
        AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.defaultClient();
        try {
            GetRoleResult role = iam.getRole(getRole);
            return Optional.of(role);
        } catch (NoSuchEntityException e) {
            return Optional.empty();

        }

    }


    public void createBuildProject(Role role, int failures) throws InterruptedException {
        AWSCodeBuild codeBuild = AWSCodeBuildClientBuilder.defaultClient();
        try {
            CreateProjectRequest createProjectRequest = new CreateProjectRequest();
            createProjectRequest.setName(buildProjectName);

            ProjectSource projectSource = new ProjectSource();
            projectSource.setType(SourceType.CODEPIPELINE);
            createProjectRequest.setSource(projectSource);
            createProjectRequest.setServiceRole(role.getArn());

            ProjectArtifacts artifacts = new ProjectArtifacts();
            artifacts.setName("JavaPipilineBuildArtifact");
            artifacts.setType(ArtifactsType.CODEPIPELINE);
            createProjectRequest.setEnvironment(setEnvBuildEnvironment());
            createProjectRequest.setArtifacts(artifacts);

            codeBuild.createProject(createProjectRequest);
        } catch (ResourceAlreadyExistsException e) {
            codeBuild.deleteProject(new DeleteProjectRequest().withName(buildProjectName));
            createBuildProject(role, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Thread.sleep(2000);
            if (failures < 10) {
                createBuildProject(role, failures + 1);
            }

        }

    }


    public ProjectEnvironment setEnvBuildEnvironment() {
        ProjectEnvironment env = new ProjectEnvironment()
            .withComputeType(ComputeType.BUILD_GENERAL1_SMALL)
            .withType(EnvironmentType.LINUX_CONTAINER)
            .withImage("aws/codebuild/eb-java-8-amazonlinux-64:2.4.3");

        return env;
    }


    public void addSourceStage(List<StageDeclaration> stageDeclarations, Role role) {

        StageDeclaration stageDeclaration = new StageDeclaration();
        stageDeclaration.setName("JavaTestCodeCommit");
        ActionDeclaration action = new ActionDeclaration();

        ActionTypeId actionType = new ActionTypeId()
            .withCategory(ActionCategory.Source)
            .withProvider("GitHub")
            .withVersion("1")
            .withOwner(ActionOwner.ThirdParty);
//        action.setRoleArn(role.getArn());
        Map<String, String> configuration = new HashMap<>();
        configuration.put("Owner", "BIBSYSDEV");
        configuration.put("Repo", "authority-registry");
        configuration.put("Branch", "master");
        configuration.put("OAuthToken", getEnvVariable("oauth"));
        action.setConfiguration(configuration);
        action.setName("JavaActionCodeCommit");
        action.setActionTypeId(actionType);
        OutputArtifact outputArtifact = new OutputArtifact();
        outputArtifact.setName("javatestCodeCommitOutput");
        action.setOutputArtifacts(Collections.singleton(outputArtifact));
        List<ActionDeclaration> actionDeclarations = new ArrayList<>();
        actionDeclarations.add(action);
        stageDeclaration.setActions(actionDeclarations);

        stageDeclaration.setActions(actionDeclarations);
        stageDeclarations.add(stageDeclaration);

    }

    public void addBuildStage(List<StageDeclaration> stageDeclarations) {
        StageDeclaration stageDeclaration = new StageDeclaration();
        stageDeclaration.setName(buildProjectName);
        ActionDeclaration action = new ActionDeclaration();
        action.setInputArtifacts(
            Collections.singleton(new InputArtifact().withName("javatestCodeCommitOutput")));

        ActionTypeId actionType = new ActionTypeId()
            .withCategory(ActionCategory.Build)
            .withOwner("AWS")
            .withProvider("CodeBuild")
            .withVersion("1")
            .withOwner(ActionOwner.AWS);
        action.setName("JavaActionCodeBuild");
        HashMap<String, String> configuration = new HashMap<>();
        configuration.put("ProjectName", buildProjectName);
        action.setConfiguration(configuration);
        action.setActionTypeId(actionType);
        List<ActionDeclaration> actionDeclarations = new ArrayList<>();
        actionDeclarations.add(action);
        stageDeclaration.setActions(actionDeclarations);

        stageDeclaration.setActions(actionDeclarations);
        stageDeclarations.add(stageDeclaration);
    }


    private Role createRole() throws IOException {
        AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.defaultClient();

        String roleName = "Unit_CodePipelineBuilder";
        List<String> policies = listDefaultAmazonPolicies();

        GetRoleRequest getRole = new GetRoleRequest().withRoleName(roleName);
        deleteExistingRole(getRole);

        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest.setRoleName(roleName);
        String assumeRolePolicyDocument = ioUtils
            .removeMultipleWhiteSpaces(ioUtils.resourceAsString(
                Paths.get("policies", "assumeRolePolicy.json")));
        createRoleRequest.setAssumeRolePolicyDocument(assumeRolePolicyDocument);
        iam.createRole(createRoleRequest);

        policies.forEach(p -> attacheRolePolicy(iam, roleName, p));

        Role role = iam.getRole(getRole).getRole();

        PutRolePolicyRequest inlinePolicy = bucketAccessInlinePolicy(roleName);

        iam.putRolePolicy(inlinePolicy);

        return role;

    }

}
