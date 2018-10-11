package no.bibsys.cloudformation;

import no.bibsys.Application;

public abstract class ConfigurationTests {

    String projectName="aVeryLongProjectName";
    String branchName="aBranch";
    Application application=new Application();
    PipelineStackConfiguration conf=application.
        pipelineStackConfiguration(projectName,branchName,"repoName","repoOwner");

    protected String randomId=conf.getRandomId();
    protected String projectId=conf.getProjectId();
    protected String shortBranch=conf.getShortBranch();


}
