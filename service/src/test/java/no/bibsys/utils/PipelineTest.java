package no.bibsys.utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.utils.stacks.StackWiper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Disabled
public class PipelineTest {


    private String branchName = "master";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";


    @Tag("UtilityMethod")
    @Test
    public void createStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.createStacks();
    }


    @Tag("UtilityMethod")
    @Test
    public void deleteStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.wipeStacks();

    }


    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, branchName);
        return new Application(githubConf);
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllBuckets() throws IOException {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        Application application=initApplication();
        StackWiper stackWiper=new StackWiper(application.getPipelineStackConfiguration());
        client.listBuckets().stream().forEach(bucket -> {
            stackWiper.deleteBucket(bucket.getName(),client);

        });
    }

    private void checkIfEmptyAndDelete(AmazonS3 client, Bucket bucket) {
        boolean bucketIsEmpty = client.listObjects(bucket.getName()).getObjectSummaries().isEmpty();

        if (true) {
            client.deleteBucket(bucket.getName());
        }
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllTables() throws IOException {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        client.listTables().getTableNames().stream().filter(table -> table.startsWith("test_")).forEach(table -> client.deleteTable(table));
    }

    @Tag("UtilityMethod")
    @Test
    public void deleteAllLogs() {
        AWSLogs logs = AWSLogsClientBuilder.defaultClient();
        logs.describeLogGroups().getLogGroups().stream().forEach(logGroup -> logs.deleteLogGroup(new DeleteLogGroupRequest().withLogGroupName(logGroup.getLogGroupName())));
    }

}
