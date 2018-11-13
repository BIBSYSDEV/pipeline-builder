package no.bibsys.cloudformation;

import java.io.IOException;
import no.bibsys.git.github.GithubConf;
import no.bibsys.utils.AmazonRestrictions;
import no.bibsys.utils.MockEnvironment;

public abstract class ConfigurationTests extends AmazonRestrictions {


    protected final String normalizedBranch;
    protected final String projectId;
    protected String branchName = "AUTREG-49_Delete_tables_from_DynamoDB_after_testing";
    protected String repoOwner = "OWNER";
    protected String repoName = "REPOSITORY_NAME";
    protected PipelineStackConfiguration conf;
    protected GithubConf githubConf;


    public ConfigurationTests() throws IOException {
        githubConf = new GithubConf(repoOwner, repoName, new MockEnvironment());
        this.conf = new PipelineStackConfiguration(githubConf,branchName);
        this.normalizedBranch = conf.getNormalizedBranchName();
        this.projectId = conf.getProjectId();
    }



}
