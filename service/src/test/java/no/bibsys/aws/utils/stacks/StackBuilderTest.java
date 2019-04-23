package no.bibsys.aws.utils.stacks;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import java.io.IOException;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

public class StackBuilderTest extends LocalStackTest {

    public StackBuilderTest() {
    }

    @Test
    public void crateStacks_notExistingStack_noException() throws Exception {
        AmazonCloudFormation acf = initializeMockCloudFormation();
        AmazonIdentityManagement mockAmazonIdentityManagement = mockIdentityManagement(pipelineStackConfiguration);

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
            mockGithubReader());

        assertThrows(AmazonCloudFormationException.class, wiper::wipeStacks);
        stackBuilder.createStacks();
    }

    @Test
    public void createStacks_existingStack_noException() throws Exception {
        AmazonCloudFormation acf = initializeMockCloudFormation();
        AmazonIdentityManagement mockAmazonIdentityManagement = mockIdentityManagement(pipelineStackConfiguration);
        StackWiper wiper = new StackWiper(
            pipelineStackConfiguration,
            initializeMockCloudFormation(),
            initializeS3(),
            initializeLambdaClient(),
            initializeMockLogsClient()
        );
        StackBuilder stackBuilder = new StackBuilder(
                wiper, pipelineStackConfiguration, acf, mockAmazonIdentityManagement, mockGithubReader());

        stackBuilder.createStacks();
    }


}
