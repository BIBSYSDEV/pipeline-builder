package no.bibsys.utils;

import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {

    private String branchName="autreg-58-openapi-lambdab";
    //private String branchName = "remove-lambdaTrustRole";
    private String repoName = "authority-registry";
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



    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, new Environment());
        return new Application(githubConf, branchName);
    }


}
