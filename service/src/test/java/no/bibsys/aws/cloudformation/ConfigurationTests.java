package no.bibsys.aws.cloudformation;

import static no.bibsys.aws.testtutils.LocalTest.mockSecretsReader;

import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.tools.AmazonNamingRestrictions;

public abstract class ConfigurationTests extends AmazonNamingRestrictions {


    protected final String normalizedBranch;
    protected final String projectId;
    //underscores not allowd in AWS
    protected static final String BRANCH_NAME_WITH_NOT_ALLOWED_CHARS = "AUTREG-131_ensure_javascript_linting_is_in_place";
    protected static final String BRANCH_NAME_WITH_ONLY_ALLOWED_CHARS = "some-branch-here";
    protected static final String NORMALIZED_BRANCH_NAME_WITH_ONLY_ALLOWED_CHARS = "some-branch-here";
    protected String repoOwner = "OWNER";
    protected String repoName = "REPOSITORY_NAME_ENV_VAR";
    protected PipelineStackConfiguration conf;
    protected final GithubConf githubConfWithProblematicBranch;
    protected final GithubConf githubConfWithEasyBranch;


    public ConfigurationTests() {
        githubConfWithProblematicBranch = new GithubConf(repoOwner, repoName,
            BRANCH_NAME_WITH_NOT_ALLOWED_CHARS, mockSecretsReader());
        githubConfWithEasyBranch = new GithubConf(repoOwner, repoName,
            BRANCH_NAME_WITH_ONLY_ALLOWED_CHARS, mockSecretsReader());
        this.conf = new PipelineStackConfiguration(githubConfWithProblematicBranch);

        this.normalizedBranch = conf.getNormalizedBranchName();
        this.projectId = conf.getProjectId();
    }



}
