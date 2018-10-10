package no.bibsys.cloudformation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import no.bibsys.Application;
import org.junit.Test;

public class PipelineStackConfigurationTests {

    Application application=new Application();
    PipelineStackConfiguration conf=application.pipelineStackConfiguration();
    private String branchName=conf.getBranchName();
    private String randomId=conf.getRandomId();
    private String projectId=conf.getProjectId();
    private String shortBranch=conf.getShortBranch();

    @Test
    public void pipelineStacknameShouldContainProjectName(){
        assertThat(conf.getPipelineStackName(),containsString(projectId));
    }

    @Test
    public void pipelineStackNameShouldContainBranchName(){
        assertThat(conf.getPipelineStackName(),containsString(branchName));
    }

    @Test
    public void createStackRoleSHouldHaveARandomId(){
        assertThat(conf.getCreateStackRoleName(),containsString(randomId));
    }

    @Test
    public void pipelineRoleShouldHaveARandomId(){
        assertThat(conf.getPipelineRoleName(), containsString(randomId));
    }

    @Test
    public void bucketNameShouldContainProjectIdAndBranch(){
        assertThat(conf.getBucketName(),containsString(projectId));
        assertThat(conf.getBucketName(),containsString(shortBranch));

    }

}
