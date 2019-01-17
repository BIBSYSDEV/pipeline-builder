package no.bibsys.utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.utils.stacks.StackWiper;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {


    private String branchName = "move-to-sparkle";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";


    @Test
    @Ignore
    public void createStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.createStacks();
    }

    @Test
    @Ignore
    public void deleteStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.wipeStacks();

    }


    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, branchName);
        return new Application(githubConf);
    }



    @Test
    @Ignore
    public void deleteAllBuckets()  {
        AmazonS3 client= AmazonS3ClientBuilder.defaultClient();
        client.listBuckets().stream().forEach(bucket->{
            checkIfEmptyAndDelete(client, bucket);
        });
    }

    private void checkIfEmptyAndDelete(AmazonS3 client, Bucket bucket) {
        boolean bucketIsEmpty= client
            .listObjects(bucket.getName())
            .getObjectSummaries().isEmpty();

        if(bucketIsEmpty){
            client.deleteBucket(bucket.getName());
        }
    }


    @Test
    @Ignore
    public void deleteAllTables() throws IOException {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        client.listTables().getTableNames().stream().filter(table->table.startsWith("test_")).forEach(table->client.deleteTable(table));
    }


    @Test
    @Ignore
    public void deleteAllLogs(){
        AWSLogs logs= AWSLogsClientBuilder.defaultClient();
        logs.describeLogGroups().getLogGroups().stream()
            .forEach(logGroup -> logs.deleteLogGroup(
                new DeleteLogGroupRequest().withLogGroupName(logGroup.getLogGroupName())));
    }




}
