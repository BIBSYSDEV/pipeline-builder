package no.bibsys.cloudformation;

import no.bibsys.Application;
import org.junit.Test;

public class PipelineStackConfigurationTests {

    Application application=new Application();
    PipelineStackConfiguration conf=application.pipelineStackConfiguration();
    String branchName=conf.getBranchName();
    String randomId=conf.getRandomId();

    @Test
    public void pipelineStackNameShouldContainBranchName(){
        conf.getPipelineStackName().contains(branchName);
    }

    @Test
    public void createStackRoleSHouldHaveARandomId(){
        conf.getCreateStackRoleName().contains(randomId);
    }

}
