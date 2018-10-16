package no.bibsys.utils;

import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.cloudformation.ConfigurationTests;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest extends ConfigurationTests {

    private String projectName = "dynapipe";
    private String branchName = "java-pipeline-2";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";



    @Test
    @Ignore
    public void testTemplate() throws IOException {
        Application application = new Application(new Environment());

        application.withBranch(branchName)
            .withProjectName(projectName)
            .withRepoName(repoName)
            .withRepoOwner(repoOwner)
            .createStacks();

    }


    @Test
    @Ignore
    public void deleteStacks() throws IOException {
        Application application = new Application(new Environment());
        application.withBranch(branchName)
            .withProjectName(projectName)
            .withRepoName(repoName)
            .withRepoOwner(repoOwner)
            .wipeStacks();

    }




}
