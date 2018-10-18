package no.bibsys.utils;

import java.io.IOException;
import no.bibsys.Application;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {

    private String branchName = "master";
    private String repoName = "authority-registry";
    private String repoOwner = "BIBSYSDEV";



    @Test
    @Ignore
    public void testTemplate() throws IOException {
        Application application = new Application(new Environment());

        application.withBranch(branchName)
            .withRepoName(repoName)
            .withRepoOwner(repoOwner)
            .createStacks();

    }


    @Test
    @Ignore
    public void deleteStacks() throws IOException {
        Application application = new Application(new Environment());
        application.withBranch(branchName)

            .withRepoName(repoName)
            .withRepoOwner(repoOwner)
            .wipeStacks();

    }




}
