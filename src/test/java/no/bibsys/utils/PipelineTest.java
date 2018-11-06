package no.bibsys.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.Application;
import no.bibsys.cloudformation.Stage;
import no.bibsys.git.github.GithubConf;
import no.bibsys.handler.InitHandler;
import no.bibsys.handler.requests.PublishApi;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PipelineTest {

    private String branchName = "autreg-58-openapi-lambda";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";
    private String swaggerApiKey= "65485ae0-e16e-4605-ac84-8f300873df25";





    @Test
    @Category(DoNotRunTest.class)
    public void createStacks() throws IOException {
        Application application = initApplication();
        application.createStacks();
    }

    @Test
    @Category(DoNotRunTest.class)
    public void deleteStacks() throws IOException {
        Application application = initApplication();
        application.wipeStacks();

    }


    @Test
    @Category(DoNotRunTest.class)
    public void publishAPi() throws IOException, URISyntaxException {
        InitHandler initHandler=new InitHandler();
        PublishApi publishApi=new PublishApi();
        publishApi.setApiId("small-api");
        publishApi.setApiVersion("2");
        publishApi.setStage(Stage.FINAL);
        publishApi.setSwaggerOrganization("Unit3");
        publishApi.setBranch(branchName);
        publishApi.setOwner("BIBSYSDEV");
        publishApi.setRepository(repoName);
        publishApi.setSwaggetHubApiKey(swaggerApiKey);
        ObjectMapper mapper = JsonUtils.newJsonParser();
        String publishApiJson=mapper.writeValueAsString(publishApi);

        String output = initHandler.processInput(publishApi, null);
        System.out.println(output);
    }

    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, new Environment());
        return new Application(githubConf,branchName);
    }








}
