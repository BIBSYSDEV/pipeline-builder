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

    private static final String AWS_REGION = "awsRegion";
    private static final String GITHUB_OWNER_PROPERTY = "owner";
    private static final String GITHUB_REPOSITORY_PROPERTY = "repository";
    private static final String GIT_BRANCH_PROPERTY = "branch";
    private static final String CODEPIEPINE_ACTION = "action";
    private static final String ABSENT_OWNER_ERROR_MEESSAGE = "System property \"owner\" is not set";
    private static final String ABSENT_REPOSITORY_MESSAGE = "System property \"repository\" is not set";
    private static final String ABSENT_BRANCH_ERROR_MESSAGE = "System property \"branch\" is not set";
    private static final String VALID_VALUES_FOR_ACTION_MESSAGE = "Valid values: create,delete";
    private static final String INVALID_ACTION_VALUE_MESSAGE = "System property \"action\" is not set\n";
    private static final String ABSENT_ACTION_VALUE_MESSAGE1 = INVALID_ACTION_VALUE_MESSAGE;
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
        if (Action.CREATE.equals(Action.fromString(action))) {
            application.createStacks(cloudFormation);
        } else if (Action.DELETE.equals(Action.fromString(action))) {
            application.wipeStacks();
        }
    }

    @SuppressWarnings("PMD")
    public static void main(String... args) throws IOException {

        String repoOwner = System.getProperty(GITHUB_OWNER_PROPERTY);
        Preconditions.checkNotNull(repoOwner, ABSENT_OWNER_ERROR_MEESSAGE);
        String repository = System.getProperty(GITHUB_REPOSITORY_PROPERTY);
        Preconditions.checkNotNull(repository, ABSENT_REPOSITORY_MESSAGE);
        String branch = System.getProperty(GIT_BRANCH_PROPERTY);
        Preconditions.checkNotNull(branch, ABSENT_BRANCH_ERROR_MESSAGE);
        String action = System.getProperty(CODEPIEPINE_ACTION);
        String awsRegigon = System.getProperty(AWS_REGION);
        String message = ABSENT_ACTION_VALUE_MESSAGE1 + VALID_VALUES_FOR_ACTION_MESSAGE;
        Preconditions.checkNotNull(action, message);

        Region region = Region.getRegion(Regions.fromName(awsRegigon));
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
