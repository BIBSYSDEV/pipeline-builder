package no.bibsys;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.cloudformation.model.Parameter;
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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.bibsys.codebuild.CodeBuild;
import no.bibsys.role.PipelineRole;
import no.bibsys.source.GithubSource;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest implements EnvUtils {


  private final String rolename="Unit_CodePipelineBuilder";
  private final String s3Bucket = "java-pipeline";
  private final String pipelineName = "testJavaPipeline";
  private final String branchName="master";
  private final String projectName="emne-test";



  ConfigParseOptions options= ConfigParseOptions.defaults();




  private CodeBuild codeBuild=new CodeBuild(projectName,branchName,s3Bucket);

  private IOUtils ioUtils=new IOUtils();
  private final  GithubCredentials githubCredentials;

  public PipelineTest(){

    this.githubCredentials = new GithubCredentials(
        "BIBSYSDEV",
        "authority-registry",
        "master"
    );
  }




  @Test
  public void  testTemplate() throws IOException, InterruptedException {

    deleteStack();
//    Thread.sleep(7000);
    AmazonCloudFormation cf= AmazonCloudFormationClientBuilder.defaultClient();


    CreateStackRequest stack=new CreateStackRequest();

    stack.setStackName("JavaTestStack");
    String templateBody=ioUtils.resourceAsString(Paths.get("templates","pipelineTemplate.yaml"));
    stack.setTemplateBody(templateBody);
    List<Parameter> parameters=new ArrayList<>();
    parameters.add(new Parameter().withParameterKey("ProjectId").withParameterValue("JavaCloudFormationProject"));
    parameters.add(new Parameter().withParameterKey("Branch").withParameterValue("master"));
    parameters.add(new Parameter().withParameterKey("PipelineRoleName")
        .withParameterValue("CustomPipelineRole"));
    parameters.add(new Parameter().withParameterKey("PipelineBucket").withParameterValue("java-pipeline"));
    String auth=getEnvVariable("oauth");
    parameters.add(new Parameter().withParameterKey("GitHubAuth").withParameterValue(auth));

    stack.setParameters(parameters);
    stack.withCapabilities(Capability.CAPABILITY_NAMED_IAM);
    cf.createStack(stack);
  }



  private void deleteStack(){
    AmazonCloudFormation cf= AmazonCloudFormationClientBuilder.defaultClient();
    DeleteStackRequest delete=new DeleteStackRequest().withStackName("JavaTestStack");
    cf.deleteStack(delete);

  }

  @Ignore
  public void testPipeline() throws IOException, InterruptedException {

    PipelineRole pipelineRole=new PipelineRole(projectName,branchName);
    Role role = pipelineRole.createPipelineRole(rolename);
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

    GithubSource githubSource=new GithubSource(githubCredentials,role,projectName);
    StageDeclaration sourceStage=githubSource.addSourceStage();
    stageDeclarations.add(sourceStage);

    StageDeclaration buildStage = codeBuild.addBuildStageToPipeline(githubSource.getArtifactName());
    stageDeclarations.add(buildStage);

    stageDeclarations.add(testStack(codeBuild,role));

    return stageDeclarations;
  }



  private StageDeclaration testStack(CodeBuild codeBuild,Role role){
    StageDeclaration stageDeclaration=new StageDeclaration();
    stageDeclaration.setName("TestStackStage");
    ActionDeclaration actionDeclaration=new ActionDeclaration();
    actionDeclaration.setName("TestStack");

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

  private String branchDescription(){
    return String.format("%s_%s",projectName,branchName);
  }

//  public void deployTestEnvironment(){
//
//
//    AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
//    CreateStackSetRequest stackSetRequest=new CreateStackSetRequest();
////    CreateChangeSetRequest
//    CreateStackRequest createStackRequest=new CreateStackRequest();
//    createStackRequest.withCapabilities("CAPABILITY_IAM");
//    createStackRequest.setOnFailure(OnFailure.ROLLBACK);
//
//
//
//  }
















}
