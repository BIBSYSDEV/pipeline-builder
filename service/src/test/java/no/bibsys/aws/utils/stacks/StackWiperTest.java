package no.bibsys.aws.utils.stacks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import java.util.ArrayList;
import java.util.List;
import no.bibsys.aws.testtutils.LocalStackWipingTest;
import org.junit.jupiter.api.Test;

public class StackWiperTest extends LocalStackWipingTest {

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
    void wipeStacks_pipelineStackConfiguration_noException() {
        stackWiper.wipeStacks();
    }
}
