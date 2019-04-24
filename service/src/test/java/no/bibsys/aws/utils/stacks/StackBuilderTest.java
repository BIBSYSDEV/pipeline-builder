package no.bibsys.aws.utils.stacks;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;

public class StackBuilderTest extends LocalStackTest {

    public StackBuilderTest() {
    }

    @Test
    public void createStacks_notExistingStack_noException() throws Exception {
        AmazonCloudFormation acf = mockCloudFormationWithStack();
        AmazonIdentityManagement mockAmazonIdentityManagement = mockIdentityManagement(
            pipelineStackConfiguration);

        StackWiper wiper = new StackWiper(
            pipelineStackConfiguration,
            mockCloudFormationwithNoStack(),
            initializeS3(),
            initializeLambdaClient(),
            initializeMockLogsClient()
        );
        StackBuilder stackBuilder = new StackBuilder(
            wiper,
            pipelineStackConfiguration,
            acf,
            mockAmazonIdentityManagement,
            mockGithubReader().setGitHubConf(mockGithubConf()));

        assertThrows(AmazonCloudFormationException.class, wiper::wipeStacks);
        stackBuilder.createStacks();
    }

    @Test
    public void createStacks_existingStack_noException() throws Exception {
        AmazonCloudFormation acf = mockCloudFormationWithStack();
        AmazonIdentityManagement mockAmazonIdentityManagement = mockIdentityManagement(
            pipelineStackConfiguration);
        StackWiper wiper = new StackWiper(
            pipelineStackConfiguration,
            mockCloudFormationWithStack(),
            initializeS3(),
            initializeLambdaClient(),
            initializeMockLogsClient()
        );
        StackBuilder stackBuilder = new StackBuilder(
            wiper, pipelineStackConfiguration, acf, mockAmazonIdentityManagement,
            mockGithubReader().setGitHubConf(mockGithubConf()));

        stackBuilder.createStacks();
    }

}
