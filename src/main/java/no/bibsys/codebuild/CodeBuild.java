package no.bibsys.codebuild;

import com.amazonaws.services.codebuild.AWSCodeBuild;
import com.amazonaws.services.codebuild.AWSCodeBuildClientBuilder;
import com.amazonaws.services.codebuild.model.ArtifactsType;
import com.amazonaws.services.codebuild.model.ComputeType;
import com.amazonaws.services.codebuild.model.CreateProjectRequest;
import com.amazonaws.services.codebuild.model.DeleteProjectRequest;
import com.amazonaws.services.codebuild.model.EnvironmentType;
import com.amazonaws.services.codebuild.model.EnvironmentVariable;
import com.amazonaws.services.codebuild.model.EnvironmentVariableType;
import com.amazonaws.services.codebuild.model.ProjectArtifacts;
import com.amazonaws.services.codebuild.model.ProjectEnvironment;
import com.amazonaws.services.codebuild.model.ProjectSource;
import com.amazonaws.services.codebuild.model.ResourceAlreadyExistsException;
import com.amazonaws.services.codebuild.model.SourceType;
import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionDeclaration;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.ActionTypeId;
import com.amazonaws.services.codepipeline.model.InputArtifact;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.identitymanagement.model.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CodeBuild {

  private AWSCodeBuild codeBuild = AWSCodeBuildClientBuilder.defaultClient();


  private final String buildProjectName;
  private final String s3Bucket;



  private final String outputArtifactName;

  public CodeBuild(String projectName,String branchName,String s3Bucket) {
    this.buildProjectName = String.format("CodeBuild_%s_%s",projectName,branchName);
    this.s3Bucket=s3Bucket;
    this.outputArtifactName =String.format("%s_%s_CodeBuild_artifact",projectName,branchName);
  }



  public StageDeclaration addBuildStageToPipeline(String inputArtifactName) {
    StageDeclaration stageDeclaration = new StageDeclaration();
    stageDeclaration.setName(buildProjectName);

    ActionDeclaration action = new ActionDeclaration();
    action.setName("CodeBuildAction");

    action.setInputArtifacts(
        Collections.singleton(new InputArtifact().withName(inputArtifactName)));

    action.setActionTypeId(defaultActionTypeId());

    action.setConfiguration(codeBuildConfigurationMap());

    List<ActionDeclaration> actionDeclarations = new ArrayList<>();
    actionDeclarations.add(action);
    stageDeclaration.setActions(actionDeclarations);

    stageDeclaration.setActions(actionDeclarations);
    return stageDeclaration;
  }

  private HashMap<String, String> codeBuildConfigurationMap() {
    HashMap<String, String> configuration = new HashMap<>();
    configuration.put("ProjectName", buildProjectName);
    return configuration;
  }

  private ActionTypeId defaultActionTypeId() {
    return new ActionTypeId()
          .withCategory(ActionCategory.Build)
          .withProvider("CodeBuild")
          .withVersion("1")
          .withOwner(ActionOwner.AWS);
  }


  public void createBuildProjectForCodePipeline(Role role)
      throws InterruptedException {
    createBuildProjectForCodePipeline(role, outputArtifactName,0);
  }

  private  void createBuildProjectForCodePipeline(Role role,String artifactName, int failures) throws InterruptedException {

    try {
      CreateProjectRequest createProjectRequest = new CreateProjectRequest();
      createProjectRequest.setName(buildProjectName);

      ProjectSource projectSource = new ProjectSource();
      projectSource.setType(SourceType.CODEPIPELINE);
      createProjectRequest.setSource(projectSource);
      createProjectRequest.setServiceRole(role.getArn());

      ProjectArtifacts artifacts = new ProjectArtifacts();
      artifacts.setName(artifactName);
      artifacts.setType(ArtifactsType.CODEPIPELINE);
      createProjectRequest.setEnvironment(setEnvBuildEnvironment());
      createProjectRequest.setArtifacts(artifacts);

      codeBuild.createProject(createProjectRequest);

    } catch (ResourceAlreadyExistsException e) {
      codeBuild.deleteProject(new DeleteProjectRequest().withName(buildProjectName));
      createBuildProjectForCodePipeline(role, artifactName,0);
    } catch (Exception e) {
      e.printStackTrace();
      Thread.sleep(2000);
      if (failures < 10) {
        createBuildProjectForCodePipeline(role, artifactName,failures + 1);
      }

    }

  }


  private ProjectEnvironment setEnvBuildEnvironment() {
    ProjectEnvironment env = new ProjectEnvironment()
        .withComputeType(ComputeType.BUILD_GENERAL1_SMALL)
        .withType(EnvironmentType.LINUX_CONTAINER)
        .withImage("aws/codebuild/eb-java-8-amazonlinux-64:2.4.3")
        .withEnvironmentVariables(new EnvironmentVariable().withName("S3_BUCKET")
            .withValue(s3Bucket).withType(EnvironmentVariableType.PLAINTEXT));

    return env;
  }

  public String getOutputArtifactName() {
    return outputArtifactName;
  }


  private void deleteCodeBuildProject(){
    codeBuild.deleteProject(new DeleteProjectRequest().withName(buildProjectName));

  }





}
