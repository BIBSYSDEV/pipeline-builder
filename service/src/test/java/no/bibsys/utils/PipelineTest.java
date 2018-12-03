package no.bibsys.utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {


    private String branchName = "master";
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




}
