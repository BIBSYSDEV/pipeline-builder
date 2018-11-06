package no.bibsys.utils;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionDeclaration;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.ActionTypeId;
import com.amazonaws.services.codepipeline.model.ArtifactStore;
import com.amazonaws.services.codepipeline.model.ArtifactStoreType;
import com.amazonaws.services.codepipeline.model.CreatePipelineRequest;
import com.amazonaws.services.codepipeline.model.OutputArtifact;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.Application;
import no.bibsys.cloudformation.Stage;
import no.bibsys.git.github.GithubConf;
import no.bibsys.handler.InitHandler;
import no.bibsys.handler.requests.PublishApi;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {

    private String branchName = "autreg-58-openapi-lambdab";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";
    private String swaggerApiKey = "65485ae0-e16e-4605-ac84-8f300873df25";


    @Test
    @Ignore
    public void createStacks() throws IOException {
        Application application = initApplication();
        application.createStacks();
    }

    @Test
    @Ignore
    public void deleteStacks() throws IOException {
        Application application = initApplication();
        application.wipeStacks();

    }


    @Test
    @Ignore
    public void foo() {
        AWSCodePipeline client = AWSCodePipelineClientBuilder.defaultClient();

        PipelineDeclaration pipeline = new PipelineDeclaration().withName("TestPipeline");

        pipeline.withArtifactStore(new ArtifactStore().withLocation("orestis").withType(
            ArtifactStoreType.S3));
        pipeline.withRoleArn("arn:aws:iam::933878624978:role/Temporaryr");
        Map<String, String> sourceConfig = new HashMap<>();
        sourceConfig.put("Owner", "!Ref GithubOwner");
        sourceConfig.put("Repo", "Githuhrepo");
        sourceConfig.put("OAuthToken", "GithubAuth");
        sourceConfig.put("Branch", "branch");

        ActionTypeId sourceActionTypeId = new ActionTypeId().withCategory(ActionCategory.Source)
            .withOwner("AWS").withProvider("GitHub").withOwner(ActionOwner.ThirdParty)
            .withVersion("1");
        ActionDeclaration sourceAction = new ActionDeclaration()
            .withActionTypeId(sourceActionTypeId).withName("Source")
            .withOutputArtifacts(new OutputArtifact().withName("gitArtifact"))
            .withConfiguration(sourceConfig);
        StageDeclaration sourceStage = new StageDeclaration().withName("SourceStage")
            .withActions(sourceAction);

        Map<String, String> lambdaConfig = new HashMap<>();
        lambdaConfig.put("FunctionName", "somename");
        ActionDeclaration action = new ActionDeclaration().withActionTypeId(
            new ActionTypeId().withCategory(ActionCategory.Invoke)
                .withOwner("AWS").withProvider("Lambda")
                .withVersion("1"))
            .withName("InitializeAction")
            .withConfiguration(lambdaConfig);
        StageDeclaration stage = new StageDeclaration().withActions(action)
            .withName("testInit");

        pipeline.withStages(sourceStage, stage);
        CreatePipelineRequest createPipelineRequest = new CreatePipelineRequest()
            .withPipeline(pipeline);
        client.createPipeline(createPipelineRequest);
    }


    @Test
    @Ignore
    public void publishAPi() throws IOException, URISyntaxException {
        InitHandler initHandler = new InitHandler();
        PublishApi publishApi = new PublishApi();
        publishApi.setApiId("small-api");
        publishApi.setApiVersion("2");
        publishApi.setStage(Stage.FINAL);
        publishApi.setSwaggerOrganization("Unit3");
        publishApi.setBranch(branchName);
        publishApi.setOwner("BIBSYSDEV");
        publishApi.setRepository(repoName);
        publishApi.setSwaggetHubApiKey(swaggerApiKey);
        ObjectMapper mapper = JsonUtils.newJsonParser();
        String publishApiJson = mapper.writeValueAsString(publishApi);

        String output = initHandler.processInput(publishApi, null);
        System.out.println(output);
    }

    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, new Environment());
        return new Application(githubConf, branchName);
    }


}
