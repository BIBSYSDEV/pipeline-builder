package no.bibsys.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {


    private String branchName = "autreg-52-update-route53-dynamically";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";
    private SwaggerHubInfo swaggerHubInfo=new SwaggerHubInfo("small-api","1.0","axthosarouris");


    @Test
    @Ignore
    public void createStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.createStacks();
    }

    @Test
    @Ignore
    public void deleteStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.wipeStacks();

    }


    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, branchName);
        return new Application(githubConf);
    }


}
