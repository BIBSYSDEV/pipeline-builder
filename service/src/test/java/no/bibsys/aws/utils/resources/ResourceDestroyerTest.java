package no.bibsys.aws.utils.resources;

import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;

class ResourceDestroyerTest extends LocalStackTest {

    private static final String STACK_NAME = TEST_STACK;
    private static final String RECORD_SET_NAME = "recor.dSet.Name.";
    private static final String ZONE_NAME = "zone.Name.";
    private static final String GIT_OWNER = "owner";
    private static final String GIT_REPOSITORY = "gitRepository";
    private static final String BRANCH = "branch";

    @Test
    public void destory_stack_noException() throws IOException, URISyntaxException {
        StaticUrlInfo staticUrlInfo = new StaticUrlInfo(ZONE_NAME, RECORD_SET_NAME, Stage.TEST);
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(
            "apiId",
            "apiVersion",
            "swaggerOrg",
            mockSecretsReader());
        GitInfo gitInfo = new GithubConf(GIT_OWNER, GIT_REPOSITORY, BRANCH, mockSecretsReader());
        ResourceDestroyer destroyer = new ResourceDestroyer(
            STACK_NAME,
            staticUrlInfo,
            new SwaggerHubConnectionDetails(swaggerHubInfo, mockSecretsReader()),
            Stage.TEST,
            gitInfo,
            initializeMockCloudFormation(),
            initializeAmazonApiGateway(),
            initializeRoute53Client(staticUrlInfo.getZoneName())

        );
        destroyer.destroy();
    }
}
