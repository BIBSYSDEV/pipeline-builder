package no.bibsys.aws.utils.stacks;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import java.io.IOException;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;

public class StackBuilderTest extends LocalStackTest {

    public StackBuilderTest() {
    }

    @Test
    public void crateStacks_notExistingStack_noException() throws IOException {
        AmazonCloudFormation acf = initializeMockCloudFormation();
        StackWiper wiper = new StackWiper(
            pipelineStackConfiguration,
            mockCloudFormationwithNoStack(),
            initializeS3(),
            initializeLambdaClient(),
            initializeMockLogsClient()
        );
        StackBuilder stackBuilder = new StackBuilder(wiper, pipelineStackConfiguration, acf);

        assertThrows(AmazonCloudFormationException.class, wiper::wipeStacks);
        stackBuilder.createStacks();
    }
}
