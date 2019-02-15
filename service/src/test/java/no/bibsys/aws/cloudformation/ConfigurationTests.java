package no.bibsys.aws.cloudformation;

import static no.bibsys.aws.testtutils.LocalTest.mockSecretsReader;

import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.tools.AmazonNamingRestrictions;

public abstract class ConfigurationTests extends AmazonNamingRestrictions {


    protected final String normalizedBranch;
    protected final String projectId;
    protected String branchName = "AUTREG-131_ensure_javascript_linting_is_in_place";
    protected String repoOwner = "OWNER";
    protected String repoName = "REPOSITORY_NAME_ENV_VAR";
    protected PipelineStackConfiguration conf;
    protected GithubConf githubConf;


    public ConfigurationTests() {
        githubConf = new GithubConf(repoOwner, repoName, branchName,mockSecretsReader());
        this.conf = new PipelineStackConfiguration(githubConf);
        this.normalizedBranch = conf.getNormalizedBranchName();
        this.projectId = conf.getProjectId();
    }



}
