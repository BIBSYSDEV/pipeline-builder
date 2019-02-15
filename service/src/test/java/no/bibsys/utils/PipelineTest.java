package no.bibsys.utils;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
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

    private String branchName = "master";
    private String repoName = "authority-registry";
    private String repoOwner = "BIBSYSDEV";
    private SecretsReader secretsReader;

    public PipelineTest() {
        Environment env = new Environment();
        String secretName = env.readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_NAME);
        String secretKey = env.readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_KEY);
        Region region = Regions.getCurrentRegion();
        secretsReader = new AWSSecretsReader(secretName, secretKey, region);
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
        return new Application(githubConf);
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllBuckets() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        Application application = initApplication();
        StackWiper stackWiper = new StackWiper(application.getPipelineStackConfiguration());
        client.listBuckets().stream().forEach(bucket -> {
            stackWiper.deleteBucket(bucket.getName(), client);
        });
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllTables() throws IOException {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        client.listTables().getTableNames().stream().filter(table -> table.startsWith("test_"))
            .forEach(table -> client.deleteTable(table));
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllLogs() {
        AWSLogs logs = AWSLogsClientBuilder.defaultClient();
        logs.describeLogGroups().getLogGroups().stream().forEach(
            logGroup -> logs.deleteLogGroup(
                new DeleteLogGroupRequest().withLogGroupName(logGroup.getLogGroupName())));
    }
}
