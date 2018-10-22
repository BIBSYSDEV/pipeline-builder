package no.bibsys.cloudformation;

import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.utils.MockEnvironment;
import org.junit.Test;

public class PipelineConfigurationTest {

    Application application=new Application(new MockEnvironment())
        .withRepoOwner("BIBSYSDEV")
        .withRepoName("authority-registry")
        .withBranch("autreg-54-do-not-delete-prod-stack");

    @Test
    public void test() throws IOException {
        PipelineStackConfiguration config = application.pipelineStackConfiguration();
        PipelineConfiguration pipelineConfig=config.getPipelineConfiguration();
        System.out.print(pipelineConfig.toString());


    }

}
