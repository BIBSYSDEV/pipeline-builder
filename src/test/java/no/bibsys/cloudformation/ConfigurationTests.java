package no.bibsys.cloudformation;

import java.io.IOException;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.utils.AmazonRestrictions;
import no.bibsys.utils.Environment;

public abstract class ConfigurationTests extends AmazonRestrictions {

    protected final String projectId;
    protected String shortBranch;
    String branchName = "AUTREG-49_Delete_tables_from_DynamoDB_after_testing";
    Application application;
    PipelineStackConfiguration conf;
    private Environment environment = new Environment() {
        @Override
        public Optional<String> readEnvOpt(String variableName) {
            return Optional.of("env-variable");
        }

    };


    protected ConfigurationTests() {
        try {

            application = new Application(environment)
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
