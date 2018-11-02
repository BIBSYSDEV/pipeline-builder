package no.bibsys.utils;

import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PipelineTest {

    private String branchName = "autreg-58-openapi-lambda";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";




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

    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, new Environment());
        GithubReader githubReader = new GithubReader(new RestReader(githubConf), branchName);
        return new Application(githubReader);
    }








}
