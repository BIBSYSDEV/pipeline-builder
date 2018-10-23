package no.bibsys.utils;

import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {

    private String branchName = "autreg-54-dont-delete-prod";
    private String repoName = "authority-registry";
    private String repoOwner = "BIBSYSDEV";



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
        GithubReader githubReader = new GithubReader(new RestReader(githubConf), branchName);
        return new Application(githubReader);
    }


}
