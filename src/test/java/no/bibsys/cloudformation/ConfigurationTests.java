package no.bibsys.cloudformation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import no.bibsys.utils.AmazonRestrictions;
import no.bibsys.utils.MockEnvironment;
import org.junit.Before;
import org.mockito.Mockito;

public abstract class ConfigurationTests extends AmazonRestrictions {


    protected final String normalizedBranch;
    protected final String projectId;
    private final GithubReader githubReader;
    protected String branchName = "AUTREG-49_Delete_tables_from_DynamoDB_after_testing";
    protected String repoOwner = "OWNER";
    protected String repoName = "REPOSITORY_NAME";
    protected PipelineStackConfiguration conf;
    protected GithubConf githubConf;
    protected RestReader restReader = Mockito.mock(RestReader.class);


    public ConfigurationTests() throws IOException {
        githubConf = new GithubConf(repoOwner, repoName, new MockEnvironment());
        init();
        this.githubReader = new GithubReader(restReader, branchName);
        this.conf = new PipelineStackConfiguration(githubReader);
        this.normalizedBranch = conf.getNormalizedBranchName();
        this.projectId = conf.getProjectId();
    }

    @Before
    public void init() throws IOException {
        when(restReader.readRest(anyString())).thenReturn("{\"download_url\":\"some_url\"}");
        when(restReader.getGitInfo()).thenReturn(this.githubConf);
    }


}
