package no.bibsys.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import no.bibsys.handler.InitHandler;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {

    private String branchName = "autreg-58-openapi-lambdab";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";
    private String swaggerApiKey = "";


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
    public void publishAPi() throws IOException, URISyntaxException {
        InitHandler initHandler = new InitHandler();
        ObjectMapper mapper = JsonUtils.newJsonParser();

        String output = initHandler.processInput(null, null);
        System.out.println(output);
    }

    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, new Environment());
        return new Application(githubConf, branchName);
    }


}
