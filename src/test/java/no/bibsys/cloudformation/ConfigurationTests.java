package no.bibsys.cloudformation;

import java.io.IOException;
import no.bibsys.Application;

public abstract class ConfigurationTests {

    String projectName="aVeryLongProjectName";
    String branchName="aBranch";
    Application application;
    PipelineStackConfiguration conf;

    protected String randomId;
    protected String projectId;
    protected String shortBranch;


    protected ConfigurationTests()  {
        try {
            application=new Application();
            conf=application.
                pipelineStackConfiguration(projectName,branchName,"repoName",
                    "repoOwner",false);
        } catch (IOException e) {
            conf=null;
            e.printStackTrace();
        }

        randomId=conf.getRandomId();
        projectId=conf.getProjectId();
        shortBranch=conf.getShortBranch();


    }
}
