package no.bibsys;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import no.bibsys.cloudformation.ConfigurationTests;
import no.bibsys.utils.Environment;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest extends ConfigurationTests {

    private String projectName = "dynapipe";
    private String branchName = "java-pipeline-2";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";



    @Test
    @Ignore
    public void testTemplate() throws IOException {
        Application application = new Application(new Environment());

        application.withBranch(branchName)
            .withProjectName(projectName)
            .withRepoName(repoName)
            .withRepoOwner(repoOwner)
            .createStacks();

    }


    @Test
    @Ignore
    public void deleteStacks() throws IOException {
        Application application = new Application(new Environment());
        application.deleteStacks(application
            .pipelineStackConfiguration(projectName, branchName, repoName, repoOwner));
    }




}
