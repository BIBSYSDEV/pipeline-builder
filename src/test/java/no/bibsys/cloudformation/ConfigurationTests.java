package no.bibsys.cloudformation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import no.bibsys.git.github.GithubConf;
import no.bibsys.utils.AmazonRestrictions;
import no.bibsys.utils.Environment;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public abstract class ConfigurationTests extends AmazonRestrictions {


    protected final String normalizedBranch;
    protected final String projectId;
    protected String branchName = "AUTREG-49_Delete_tables_from_DynamoDB_after_testing";
    protected String repoOwner = "OWNER";
    protected String repoName = "REPOSITORY_NAME_ENV_VAR";
    protected PipelineStackConfiguration conf;
    protected GithubConf githubConf;


    public ConfigurationTests() throws IOException {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnvOpt(anyString())).then(
            (Answer<Optional<String>>) invocation -> Optional
                .ofNullable(invocation.getArgument(0)));
        githubConf = new GithubConf(repoOwner, repoName, environment);
        this.conf = new PipelineStackConfiguration(githubConf,branchName);
        this.normalizedBranch = conf.getNormalizedBranchName();
        this.projectId = conf.getProjectId();
    }



}
