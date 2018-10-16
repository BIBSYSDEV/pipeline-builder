package no.bibsys.cloudformation;

import java.io.IOException;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.utils.Environment;

public abstract class ConfigurationTests {

    String projectName="aVeryLongProjectName";
    String branchName="aBranch";
    Application application;
    PipelineStackConfiguration conf;

    protected String randomId;
    protected String projectId;
    protected String shortBranch;

    private Environment environment = new Environment() {
        @Override
        public Optional<String> readEnvOpt(String variableName) {
            return Optional.of("env-variable");
        }

    };




    protected ConfigurationTests()  {
        try {

            application=new Application(environment);
            conf=application.
                pipelineStackConfiguration(projectName,branchName,"repoName",
                    "repoOwner");
        } catch (IOException e) {
            conf=null;
            e.printStackTrace();
        }

        randomId=conf.getRandomId();
        projectId=conf.getProjectId();
        shortBranch=conf.getShortBranch();


    }
}
