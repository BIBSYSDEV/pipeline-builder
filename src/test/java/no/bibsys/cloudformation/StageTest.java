package no.bibsys.cloudformation;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.List;
import org.junit.Test;

public class StageTest {


    @Test
    public void fromString_test_TestStage() {
        Stage s1 = Stage.fromString("test");
        Stage s2 = Stage.fromString("TEST");
        assertThat(s1, is(equalTo(Stage.TEST)));
    }


    @Test
    public void fromString_final_FinalStage() {
        Stage s1 = Stage.fromString("final");
        Stage s2 = Stage.fromString("FINAL");
        assertThat(s1, is(equalTo(Stage.FINAL)));
    }


    @Test
    public void listStrings_void_allStages() {
        List<Stage> stages = Stage.listStages();
        assertThat(stages.size(), is(equalTo(2)));
        assertThat(stages, contains(Stage.TEST, Stage.FINAL));

    }

    @Test
    public void toString_void_toString() {
        assertThat(Stage.FINAL.toString(), is(equalTo("final")));
    }


}
