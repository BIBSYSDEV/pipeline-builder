package no.bibsys;

import static com.amazonaws.regions.ServiceAbbreviations.CloudFormation;

import com.amazonaws.auth.policy.actions.CloudFormationActions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackSetRequest;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionDeclaration;
import com.amazonaws.services.codepipeline.model.ActionTypeId;
import com.amazonaws.services.codepipeline.model.ArtifactStore;
import com.amazonaws.services.codepipeline.model.CreatePipelineRequest;
import com.amazonaws.services.codepipeline.model.DeletePipelineRequest;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.identitymanagement.model.Role;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.bibsys.build.CodeBuild;
import no.bibsys.role.RoleHelper;
import no.bibsys.source.GithubSource;
import org.junit.Test;

public class PipelineTest implements EnvUtils {


  private final String rolename="Unit_CodePipelineBuilder";
  private final String s3Bucket = "java-pipeline";
  private final String pipelineName = "testJavaPipeline";
  private final String branchName="master";
  private final String buildArtifactName= "CodeBuildArtifact_"+branchName;
  private final String buildProjectName="CodeBuild_"+branchName;


  private RoleHelper roleHelper=new RoleHelper();
  private CodeBuild codeBuild=new CodeBuild(buildProjectName,buildArtifactName,s3Bucket);

  private final  GithubCredentials githubCredentials;

  public PipelineTest(){

    this.githubCredentials = new GithubCredentials(
        "BIBSYSDEV",
        "authority-registry",
        "master"
    );
  }


  @Test
  public void testPipeline() throws IOException, InterruptedException {

    Role role = roleHelper.createRole(rolename);
    codeBuild.createBuildProjectForCodePipeline(role);

    AWSCodePipeline client = AWSCodePipelineClientBuilder.defaultClient();

    try {
      client.deletePipeline(new DeletePipelineRequest().withName(pipelineName));
    } catch (Exception e) {
      e.printStackTrace();
    }
    CreatePipelineRequest request = new CreatePipelineRequest();

    PipelineDeclaration pipelineDeclaration = new PipelineDeclaration();
    pipelineDeclaration.setName(pipelineName);
    pipelineDeclaration.setRoleArn(role.getArn());
    pipelineDeclaration.setArtifactStore(new ArtifactStore()
        .withType("S3").withLocation(s3Bucket));

    List<StageDeclaration> stageDeclarations = createStageDeclarations(role);

    pipelineDeclaration.setStages(stageDeclarations);

    request.setPipeline(pipelineDeclaration);
    client.createPipeline(request);

  }

  private List<StageDeclaration> createStageDeclarations(Role role) {
    List<StageDeclaration> stageDeclarations = new ArrayList<>();

    GithubSource githubSource=new GithubSource(githubCredentials,role,"emneApp");
    StageDeclaration sourceStage=githubSource.addSourceStage();
    stageDeclarations.add(sourceStage);

    StageDeclaration buildStage = codeBuild.addBuildStage(githubSource.getArtifactName());
    stageDeclarations.add(buildStage);

    stageDeclarations.add(testStack(codeBuild,role));

    return stageDeclarations;
  }



  private StageDeclaration testStack(CodeBuild codeBuild,Role role){
    StageDeclaration stageDeclaration=new StageDeclaration();
    stageDeclaration.setName("TestStackStage");
    ActionDeclaration actionDeclaration=new ActionDeclaration();
    actionDeclaration.setName("TestStack");
//    actionDeclaration.setRoleArn();
    ActionTypeId actionTypeId=new ActionTypeId();
    actionTypeId.setCategory(ActionCategory.Deploy);
    actionTypeId.setOwner("AWS");
    actionTypeId.setVersion("1");
    actionTypeId.setProvider("CloudFormation");


    Map<String,String> configuration=new HashMap<>();
    configuration.put("ActionMode","CHANGE_SET_REPLACE");
    configuration.put("StackName","TestStack");
    configuration.put("ChangeSetName","TestStageChangeSet");
    String templatePath=String.format("%s::%s",codeBuild.getOutputArtifactName(),"template-export.yml");
    configuration.put("TemplatePath",templatePath);
    actionDeclaration.setConfiguration(configuration);

    actionDeclaration.setActionTypeId(actionTypeId);
    stageDeclaration.withActions(actionDeclaration);
    return stageDeclaration;

  }



  public void deployTestEnvironment(){


    AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
    CreateStackSetRequest stackSetRequest=new CreateStackSetRequest();
//    CreateChangeSetRequest
    CreateStackRequest createStackRequest=new CreateStackRequest();
    createStackRequest.withCapabilities("CAPABILITY_IAM");
    createStackRequest.setOnFailure(OnFailure.ROLLBACK);



  }
















}
