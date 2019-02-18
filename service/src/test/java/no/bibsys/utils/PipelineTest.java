package no.bibsys.utils;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.IOException;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.secrets.AWSSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.stacks.StackWiper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Disabled
public class PipelineTest {

    private final AmazonCloudFormation cloudFormation;
    private final AmazonS3 s3Client;
    private final AWSLambda lambdaClient;
    private static final String branchName = "master";
    private static final String repoName = "authority-registry";
    private static final String repoOwner = "BIBSYSDEV";
    private final transient AWSLogs logsClient;
    private SecretsReader secretsReader;

    public PipelineTest() {
        Environment env = new Environment();
        String secretName = env.readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_NAME);
        String secretKey = env.readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_KEY);
        Region region = Regions.getCurrentRegion();
        secretsReader = new AWSSecretsReader(secretName, secretKey, region);
        this.cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();
        this.s3Client = AmazonS3ClientBuilder.defaultClient();
        this.lambdaClient = AWSLambdaClientBuilder.defaultClient();
        this.logsClient = AWSLogsClientBuilder.defaultClient();
    }

    @Tag("UtilityMethod")
    @Test
    public void createStacks() throws IOException {
        Application application = initApplication();
        application.createStacks();
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteStacks() {
        Application application = initApplication();
        application.wipeStacks();
    }

    private Application initApplication() {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, branchName, secretsReader);
        return new Application(githubConf, cloudFormation, s3Client, lambdaClient, logsClient);
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllBuckets() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        Application application = initApplication();
        StackWiper stackWiper = new StackWiper(application.getPipelineStackConfiguration()
            , cloudFormation, s3Client, lambdaClient, logsClient);
        client.listBuckets().forEach(bucket -> stackWiper.deleteBucket(bucket.getName()));
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllTables() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        client.listTables().getTableNames().stream().filter(table -> table.startsWith("test_"))
            .forEach(client::deleteTable);
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllLogs() {
        AWSLogs logs = AWSLogsClientBuilder.defaultClient();
        logs.describeLogGroups().getLogGroups().forEach(
            logGroup -> logs.deleteLogGroup(
                new DeleteLogGroupRequest().withLogGroupName(logGroup.getLogGroupName())));
    }
}
