package no.bibsys.source;

import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionDeclaration;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.ActionTypeId;
import com.amazonaws.services.codepipeline.model.OutputArtifact;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.identitymanagement.model.Role;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.bibsys.EnvUtils;
import no.bibsys.GithubCredentials;
import no.bibsys.PipelineStep;

public class GithubSource extends PipelineStep implements EnvUtils {

  private final Role role;
  private  final GithubCredentials githubCredentials;
  private final String artifactName;

  public GithubSource(GithubCredentials credentials,
      Role role,String projectName) {

    this.githubCredentials=credentials;
    this.role = role;
    this.artifactName=String.format("%s_%s",projectName,credentials.getBranch());
  }





  public StageDeclaration addSourceStage() {
    StageDeclaration stageDeclaration = new StageDeclaration();
    String stageName= createStageName();
    stageDeclaration.setName(stageName);
    ActionDeclaration action = new ActionDeclaration();

    action.setConfiguration(configurationMap());
    action.setName("GithubPull");


    ActionTypeId actionTypeId = new ActionTypeId()
        .withCategory(ActionCategory.Source)
        .withProvider("GitHub")
        .withVersion("1")// no idea what this version is about
        .withOwner(ActionOwner.ThirdParty);


    action.setActionTypeId(actionTypeId);


    List<OutputArtifact> outputArtifacts = createOutputArtifacts();
    action.setOutputArtifacts(outputArtifacts);

    List<ActionDeclaration> actionDeclarations = new ArrayList<>();
    actionDeclarations.add(action);

    stageDeclaration.setActions(actionDeclarations);

    stageDeclaration.setActions(actionDeclarations);
    return stageDeclaration;

  }

  private List<OutputArtifact> createOutputArtifacts() {
    OutputArtifact outputArtifact = new OutputArtifact();
    outputArtifact.setName(artifactName);
    return Collections.singletonList(outputArtifact);
  }

  private Map<String, String> configurationMap() {
    Map<String, String> configuration = new HashMap<>();

    configuration.put("Owner", githubCredentials.getOwner());
    configuration.put("Repo", githubCredentials.getRepository());
    configuration.put("Branch", githubCredentials.getBranch());
    configuration.put("OAuthToken", githubCredentials.getOauth());
    return configuration;
  }

  private String createStageName() {
    return "Github_pull";
  }



  public final String getArtifactName(){
    return  this.artifactName;
  }

}
