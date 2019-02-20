package no.bibsys.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.Preconditions;
import java.io.IOException;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.utils.Action;
import no.bibsys.aws.secrets.AwsSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.stacks.StackBuilder;
import no.bibsys.aws.utils.stacks.StackWiper;

public class Application {

    private static final int INIITIAL_CAPACITY = 100;
    private final transient StackWiper wiper;

    private final transient String repoName;
    private final transient String branch;

    private final transient PipelineStackConfiguration pipelineStackConfiguration;

    public Application(GithubConf gitInfo,
        AmazonCloudFormation acf,
        AmazonS3 s3Client,
        AWSLambda lambdaClient,
        AWSLogs logsClient) {
        this.pipelineStackConfiguration = new PipelineStackConfiguration(gitInfo);
        this.repoName = gitInfo.getRepository();
        this.branch = gitInfo.getBranch();

        wiper = new StackWiper(pipelineStackConfiguration, acf, s3Client, lambdaClient, logsClient);
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
        AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        AWSLambda lambdaClient = AWSLambdaClientBuilder.defaultClient();
        AWSLogs logsClient = AWSLogsClientBuilder.defaultClient();
        Application application = new Application(gitInfo, cloudFormation, s3Client, lambdaClient,
            logsClient);
        if (action.equals(Action.CREATE)) {
            application.createStacks(cloudFormation);
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
        StringBuilder message = new StringBuilder(INIITIAL_CAPACITY);
        message.append("System property \"action\" is not set\n" + "Valid values: create,delete");
        Preconditions.checkNotNull(action, message.toString());

        Region region = Regions.getCurrentRegion();
        Environment environment = new Environment();
        String readFromGithubSecretName = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_NAME);
        String readFromGithubSecretKey = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_KEY);
        SecretsReader secretsReader = new AwsSecretsReader(readFromGithubSecretName,
            readFromGithubSecretKey, region);
        Application.run(repoOwner, repository, branch, action, secretsReader);
    }

    public PipelineStackConfiguration getPipelineStackConfiguration() {
        return pipelineStackConfiguration;
    }

    public void createStacks(AmazonCloudFormation cloudFormation) throws IOException {
        StackBuilder stackBuilder = new StackBuilder(wiper,
            pipelineStackConfiguration,
            cloudFormation);
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
