package no.bibsys;

import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codebuild.AWSCodeBuildClientBuilder;
import com.amazonaws.services.codebuild.model.CreateProjectRequest;
import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionDeclaration;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.ActionTypeId;
import com.amazonaws.services.codepipeline.model.ArtifactStore;
import com.amazonaws.services.codepipeline.model.CreatePipelineRequest;
import com.amazonaws.services.codepipeline.model.InputArtifact;
import com.amazonaws.services.codepipeline.model.OutputArtifact;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Policy;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class PipelineTest implements EnvUtils {


    IOUtils ioUtils = new IOUtils();

    @Test
    public void testPipeline() throws IOException {
        String roleArn = createPolicy();

        AWSCodePipeline client = AWSCodePipelineClientBuilder.defaultClient();
////    client.deletePipeline(new DeletePipelineRequest().withName("java-pipeline"));

        CreatePipelineRequest request = new CreatePipelineRequest();
        PipelineDeclaration pipelineDeclaration = new PipelineDeclaration();
        pipelineDeclaration.setName("testJavaPipeline");
        pipelineDeclaration.setRoleArn(roleArn);
        pipelineDeclaration.setArtifactStore(new ArtifactStore()
            .withType("S3").withLocation("java-pipeline"));

        List<StageDeclaration> stageDeclarations = new ArrayList<>();
        addSourceStage(stageDeclarations);
        addBuildStage(stageDeclarations);
        pipelineDeclaration.setStages(stageDeclarations);
        request.setPipeline(pipelineDeclaration);
        client.createPipeline(request);

    }


    public String createPolicy() throws IOException {
        AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.defaultClient();

        String roleName = "Unit_CodePipelineBuilder";
        List<String> policies = new ArrayList<>();
        policies.add("arn:aws:iam::aws:policy/AWSCodePipelineFullAccess");
        policies.add("arn:aws:iam::aws:policy/AWSCodeCommitFullAccess");


        GetRoleRequest getRole = new GetRoleRequest().withRoleName(roleName);

        Optional<GetRoleResult> getRoleResponse = getRole(getRole);
        if (getRoleResponse.isPresent()) {
            return getRoleResponse.get().getRole().getArn();
        }
        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest.setRoleName(roleName);
        String assumeRolePolicyDocument = ioUtils
            .removeMultipleWhiteSpaces(ioUtils.resourceAsString(
                Paths.get("policies", "assumeRolePolicy.json")));
        createRoleRequest.setAssumeRolePolicyDocument(assumeRolePolicyDocument);
        iam.createRole(createRoleRequest);

        policies.forEach(p->attacheRolePolicy(iam,roleName,p));

        String arn = iam.getRole(getRole).getRole().getArn();
        return arn;


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

//  public void createPipelineRole(){
//    AmazonIdentityManagement iam= AmazonIdentityManagementClientBuilder.defaultClient();
//    CreateRoleRequest createRoleRequest = new CreateRoleRequest();
//    createRoleRequest.setRoleName("Custom_Java_Pipeline");
//    CreatePolicyRequest createPolicy = new CreatePolicyRequest();
//    createPolicy.setPolicyName("custom_java_pipeline_policy");
//    createPolicy.setPolicyDocument();
//    iam.createPolicy()
//
//
//    iam.createRole()
//  }


    public void createBuildProject() {
        AWSCodeBuild codeBuild = AWSCodeBuildClientBuilder.defaultClient();
        CreateProjectRequest createProjectRequest = new CreateProjectRequest();
        createProjectRequest.setName("javaPPBuild");
        codeBuild.createProject(createProjectRequest);

    }


    public void addSourceStage(List<StageDeclaration> stageDeclarations) {

        StageDeclaration stageDeclaration = new StageDeclaration();
        stageDeclaration.setName("JavaTestCodeCommit");
        ActionDeclaration action = new ActionDeclaration();

        ActionTypeId actionType = new ActionTypeId()
            .withCategory(ActionCategory.Source)
            .withProvider("GitHub")
            .withVersion("1")
            .withOwner(ActionOwner.ThirdParty);

        Map<String, String> configuration = new HashMap<>();
        configuration.put("Owner", "BIBSYSDEV");
        configuration.put("Repo", "authority-registry");
        configuration.put("Branch","master");
        configuration.put("OAuthToken",getEnvVariable("oauth"));
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
        stageDeclaration.setName("JavaTestCodeBuild");
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
        configuration.put("ProjectName", "javaPPBuild");
        action.setConfiguration(configuration);
        action.setActionTypeId(actionType);
        List<ActionDeclaration> actionDeclarations = new ArrayList<>();
        actionDeclarations.add(action);
        stageDeclaration.setActions(actionDeclarations);

        stageDeclaration.setActions(actionDeclarations);
        stageDeclarations.add(stageDeclaration);
    }


}
