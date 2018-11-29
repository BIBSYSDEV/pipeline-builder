package no.bibsys.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.utils.resources.ResourceInitializer;
import org.junit.Test;

public class ResourceInitializerTest {

    private transient String zoneName="aws.unit.no";
    private transient String stackName="aut-reg-inf-hotfi-service-stack-test";
    private transient SwaggerHubInfo swaggerHubInfo=new SwaggerHubInfo("simple-api","1.0","axthosarouris");
    private transient String certificate="arn:aws:acm:eu-west-1:933878624978:certificate/b163e7df-2e12-4abf-ae91-7a8bbd19fb9a";


    @Test
    public void foo() throws IOException, URISyntaxException {
        ResourceInitializer resourceInitializer=new ResourceInitializer(
            zoneName,stackName,swaggerHubInfo, Stage.TEST,certificate);

        resourceInitializer.initializeStacks();

    }

}
