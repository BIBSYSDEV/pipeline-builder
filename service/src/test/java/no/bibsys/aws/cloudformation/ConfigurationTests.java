package no.bibsys.aws.cloudformation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.tools.AmazonNamingRestrictions;
import no.bibsys.aws.tools.Environment;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public abstract class ConfigurationTests extends AmazonNamingRestrictions {


    protected final String normalizedBranch;
    protected final String projectId;
    protected String branchName = "AUTREG-131_ensure_javascript_linting_is_in_place";
    protected String repoOwner = "OWNER";
    protected String repoName = "REPOSITORY_NAME_ENV_VAR";
    protected PipelineStackConfiguration conf;
    protected GithubConf githubConf;


    public ConfigurationTests() throws IOException {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnvOpt(anyString()))
                .then((Answer<Optional<String>>) invocation -> Optional.ofNullable(invocation.getArgument(0)));
        githubConf = new GithubConf(repoOwner, repoName, branchName);
        this.conf = new PipelineStackConfiguration(githubConf);
        this.normalizedBranch = conf.getNormalizedBranchName();
        this.projectId = conf.getProjectId();
    }



}
