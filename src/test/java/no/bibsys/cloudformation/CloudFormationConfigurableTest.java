package no.bibsys.cloudformation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

public class CloudFormationConfigurableTest extends ConfigurationTests {



    public CloudFormationConfigurableTest() throws IOException {
    }


    @Test
    public void normalizedBranchShouldNotExceedPredefinedLength(){
        assertThat(conf.getNormalizedBranchName().length(),
            is(not(greaterThan(CloudFormationConfigurable.NORMALIZED_BRANCH_MAX_LENGTH))));
    }


    @Test
    public void projectIdShoulNotContainedUnderscores(){
        assertThat(repoName,containsString("_"));
        assertThat(repoName,is(equalTo("REPOSITORY_NAME")));
        assertThat(projectId,not(containsString("_")));

        String[] tokens = projectId.split("-");
        Arrays.stream(tokens).forEach(token->{
                assertThat(token.length(),is(not(greaterThan(CloudFormationConfigurable.MAX_PROJECT_WORD_LENGTH))));
            });

        assertThat(projectId,is(equalTo("rep-nam")));

    }
}
