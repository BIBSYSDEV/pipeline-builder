package no.bibsys.aws.utils.stacks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.s3.AmazonS3;
import java.util.ArrayList;
import java.util.List;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;

public class StackWiperTest extends LocalStackTest {

    private StackWiper stackWiper;

    public StackWiperTest() {
        AmazonCloudFormation acf = initializeMockCloudFormation();
        AmazonS3 s3 = initializeS3();
        AWSLambda lambda = initializeLambdaClient();
        AWSLogs logsClient = initializeMockLogsClient();
        this.stackWiper = new StackWiper(pipelineStackConfiguration, acf,
            s3, lambda, logsClient);
    }


    @Test
    void deleteStacks_pielineStackConfiguration_deleteStackResults() {
        List<DeleteStackResult> results = stackWiper
            .deleteStacks();
        List<DeleteStackResult> resultList = new ArrayList<>(results);
        assertThat(resultList.isEmpty(), is((equalTo(false))));
    }

    @Test
    void deleteBuckets_pipelineStackConfiguration_noException() {
        stackWiper.deleteBuckets();
    }


    @Test
    public void wipeStacks_stackDoesNotExist_exception(){
        StackWiper stackWiper=new StackWiper(pipelineStackConfiguration,
            mockCloudFormationwithNoStack(),
            initializeS3(),
            initializeLambdaClient(),
            initializeMockLogsClient());
        assertThrows(AmazonCloudFormationException.class, stackWiper::wipeStacks);
    }

    @Test
    void wipeStacks_pipelineExists_noException() {
        stackWiper.wipeStacks();
    }


}
