package no.bibsys.utils;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.logs.model.DeleteLogStreamRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.utils.stacks.StackWiper;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {


    private String branchName = "jersey-2-authorizer";
    private String repoName = "authority-registry";
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
    public void deleteAllBuckets() throws IOException {
        AmazonS3 client= AmazonS3ClientBuilder.defaultClient();
        StackWiper stackWiper=new StackWiper(initApplication().getPipelineStackConfiguration());
        client.listBuckets().stream().forEach(bucket->{

            stackWiper.deleteBucket(bucket.getName());
        });

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
