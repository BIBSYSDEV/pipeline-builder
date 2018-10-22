package no.bibsys.cloudformation;

import java.io.IOException;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.utils.AmazonRestrictions;
import no.bibsys.utils.Environment;
import no.bibsys.utils.MockEnvironment;

public abstract class ConfigurationTests extends AmazonRestrictions {

    protected final String projectId;
    protected String shortBranch;
    String branchName = "AUTREG-49_Delete_tables_from_DynamoDB_after_testing";
    Application application;
    PipelineStackConfiguration conf;



    protected ConfigurationTests() {
        try {

            application = new Application(new MockEnvironment())
                .withBranch(branchName)
                .withRepoName("AUTHORITY-REGISTRY")
                .withRepoOwner("BIBSYSDEV");
            conf = application.pipelineStackConfiguration();
        } catch (IOException e) {
            conf = null;
            e.printStackTrace();
        }


        shortBranch = conf.getNormalizedBranchName();
        projectId = conf.getProjectId();


    }
}
