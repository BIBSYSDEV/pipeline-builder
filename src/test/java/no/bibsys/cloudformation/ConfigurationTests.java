package no.bibsys.cloudformation;

import java.io.IOException;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.utils.Environment;

public abstract class ConfigurationTests {

    protected final String projectId;
    protected String randomId;
    protected String projectName = "projectName";
    protected String shortBranch;
    String branchName = "aBranch";
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
                .withProjectName(projectName)
                .withRepoName("repoName")
                .withRepoOwner("repoOwner");
            conf = application.pipelineStackConfiguration();
        } catch (IOException e) {
            conf = null;
            e.printStackTrace();
        }

        randomId = conf.getRandomId();
        shortBranch = conf.getShortBranch();
        projectId = conf.getProjectId();


    }
}
