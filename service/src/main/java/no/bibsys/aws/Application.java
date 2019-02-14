package no.bibsys.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.common.base.Preconditions;
import java.io.IOException;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.utils.Action;
import no.bibsys.aws.secrets.AWSSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.stacks.StackBuilder;
import no.bibsys.aws.utils.stacks.StackWiper;

public class Application {

    private final transient StackWiper wiper;

    private final transient String repoName;
    private final transient String branch;

    private final transient PipelineStackConfiguration pipelineStackConfiguration;

    public Application(GithubConf gitInfo) {
        this.pipelineStackConfiguration = new PipelineStackConfiguration(gitInfo);
        this.repoName = gitInfo.getRepository();
        this.branch = gitInfo.getBranch();

        wiper = new StackWiper(pipelineStackConfiguration);
        checkNulls();
    }

    public static void run(String repoOwner,
        String repository,
        String branch,
        String action,
        SecretsReader secretsReader
    )
        throws IOException {

        GithubConf gitInfo = new GithubConf(repoOwner, repository, branch, secretsReader);
        Application application = new Application(gitInfo);
        if (action.equals(Action.CREATE)) {
            application.createStacks();
        } else if (action.equals(Action.DELETE)) {
            application.wipeStacks();
        }
    }

    public static void main(String... args) throws IOException {
        String repoOwner = System.getProperty("owner");
        Preconditions.checkNotNull(repoOwner, "System property \"owner\" is not set");
        String repository = System.getProperty("repository");
        Preconditions.checkNotNull(repository, "System property \"repository\" is not set");
        String branch = System.getProperty("branch");
        Preconditions.checkNotNull(branch, "System property \"branch\" is not set");
        String action = System.getProperty("action");
        StringBuilder message = new StringBuilder(100);
        message.append("System property \"action\" is not set\n" + "Valid values: create,delete");
        Preconditions.checkNotNull(action, message.toString());

        Region region = Regions.getCurrentRegion();
        Environment environment = new Environment();
        String readFromGithubSecretName = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_NAME);
        String readFromGithubSecretKey = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_KEY);
        SecretsReader secretsReader = new AWSSecretsReader(readFromGithubSecretName,
            readFromGithubSecretKey, region);
        Application.run(repoOwner, repository, branch, action, secretsReader);
    }

    public PipelineStackConfiguration getPipelineStackConfiguration() {
        return pipelineStackConfiguration;
    }

    public void createStacks() throws IOException {
        StackBuilder stackBuilder = new StackBuilder(wiper, pipelineStackConfiguration);
        stackBuilder.createStacks();
    }

    public void wipeStacks() {
        checkNulls();
        wiper.wipeStacks();
    }

    private void checkNulls() {
        Preconditions.checkNotNull(repoName);
        Preconditions.checkNotNull(branch);
    }
}
