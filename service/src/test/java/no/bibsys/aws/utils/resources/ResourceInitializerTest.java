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

class ResourceInitializerTest extends LocalStackTest {

    private static final String STACK_NAME = TEST_STACK;
    private static final String RECORD_SET_NAME = "record.Set.Name.";
    private static final String ZONE_NAME = "zone.Name.";
    private static final String GIT_OWNER = "owner";
    private static final String GIT_REPOSITORY = "gitRepository";
    private static final String BRANCH = "branch";
    private static final String AWS_CERTIFCATE_ARN = "aws:::certifcateARN";

    @Test
    public void initializeStacks_stackInfo_noException() throws IOException, URISyntaxException {
        StaticUrlInfo staticUrlInfo = new StaticUrlInfo(ZONE_NAME, RECORD_SET_NAME, Stage.TEST);
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(
            "apiId",
            "apiVersion",
            "swaggerOrg",
            mockSecretsReader());
        GitInfo gitInfo = new GithubConf(GIT_OWNER, GIT_REPOSITORY, BRANCH, mockSecretsReader());
        ResourceInitializer initializer = new ResourceInitializer(
            STACK_NAME,
            staticUrlInfo,
            AWS_CERTIFCATE_ARN,
            new SwaggerHubConnectionDetails(swaggerHubInfo, mockSecretsReader()),
            Stage.TEST,
            gitInfo,
            mockCloudFormationWithStack(),
            initializeAmazonApiGateway(),
            initializeRoute53Client(staticUrlInfo.getZoneName())

        );
        initializer.initializeStacks();
    }
}
