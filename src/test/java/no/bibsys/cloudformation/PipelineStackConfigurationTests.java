package no.bibsys.cloudformation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.Is.is;

import java.util.regex.Matcher;
import org.junit.Test;

public class PipelineStackConfigurationTests extends ConfigurationTests {


    @Test
    public void pipelineStacknameShouldContainProjectName() {
        assertThat(conf.getPipelineStackName(), containsString(projectId));
    }

    @Test
    public void pipelineStackNameShouldContainShortBranchName() {
        assertThat(conf.getPipelineStackName(), containsString(shortBranch));
    }

    @Test
    public void createStackRoleShouldContainShortBranch() {
        assertThat(conf.getCreateStackRoleName(), containsString(shortBranch));
    }


    @Test
    public void createStackRoleShouldContainProjectId() {
        assertThat(conf.getCreateStackRoleName(), containsString(projectId));
    }


    @Test
    public void pipelineRoleShouldContainProjectId() {
        assertThat(conf.getPipelineRoleName(), containsString(projectId));
    }


    @Test
    public void lambdaTrustRoeleShouldContainProjectId() {
        assertThat(conf.getPipelineConfiguration().getLambdaTrustRolename(), containsString(projectId));
    }


    @Test
    public void lambdaTrustRoeleShouldContainShortBranch() {
        assertThat(conf.getPipelineConfiguration().getLambdaTrustRolename(), containsString(shortBranch));
    }


    @Test
    public void pipelineRoleContainShortBranch() {
        assertThat(conf.getPipelineRoleName(), containsString(shortBranch));
    }

    @Test
    public void bucketNameShouldContainProjectIdAndBranch() {
        assertThat(conf.getBucketName(), containsString(projectId));
        assertThat(conf.getBucketName(), containsString(shortBranch));

    }

    @Test
    public void shortBrancNameShouldComplyToAmazonRestrctricions(){
        Matcher matcher=amazonPattern.matcher(conf.getShortBranch());
        assertThat(matcher.matches(),is(equalTo(true)));

    }


    @Test
    public void stackNamesShouldComplyToAmazonRestrctricions(){

        Matcher matcher=amazonPattern.matcher(conf.getPipelineStackName());
        assertThat(matcher.matches(),is(equalTo(true)));
        matcher=amazonPattern.matcher(conf.getPipelineConfiguration().getTestServiceStack());
        assertThat(matcher.matches(),is(equalTo(true)));
        matcher=amazonPattern.matcher(conf.getPipelineConfiguration().getFinalServiceStack());
        assertThat(matcher.matches(),is(equalTo(true)));

    }



}
