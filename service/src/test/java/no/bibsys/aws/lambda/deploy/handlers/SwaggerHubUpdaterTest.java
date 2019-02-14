package no.bibsys.aws.lambda.deploy.handlers;

import static no.bibsys.aws.testtutils.LocalTest.mockApiGateway;
import static no.bibsys.aws.testtutils.LocalTest.mockSecretsReader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.BranchInfo;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import org.junit.jupiter.api.Test;

public class SwaggerHubUpdaterTest {

    private static final String API_ID = "apiId";
    private static final String API_VERSION = "apiVersion";
    private static final String SWAGGER_ORG = "swaggerOrg";
    private static final String STACK_NAME = "stackName";

    @Test
    public void swaggerHubUpdater_notMasterBranch_restAPIWithStackName() throws IOException {
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(API_ID, API_VERSION, SWAGGER_ORG,
            mockSecretsReader());
        BranchInfo branchInfo = new BranchInfo(null, "notmaster");
        SwaggerHubUpdater swaggerHubUpdater = new SwaggerHubUpdater(
            mockApiGateway(),
            null,
            swaggerHubInfo,
            mockSecretsReader(),
            Stage.FINAL,
            STACK_NAME,
            branchInfo);

        SwaggerHubInfo newInfo = swaggerHubUpdater.getSwaggerHubInfo();
        assertThat(newInfo.getApiId(), is(equalTo(STACK_NAME)));
    }

    @Test
    public void swaggerHubUpdater_masterBranch_restAPIWithPredeternminedApiId() throws IOException {
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(API_ID, API_VERSION, SWAGGER_ORG,
            mockSecretsReader());
        BranchInfo branchInfo = new BranchInfo(null, "master");
        SwaggerHubUpdater swaggerHubUpdater = new SwaggerHubUpdater(
            mockApiGateway(),
            null,
            swaggerHubInfo,
            mockSecretsReader(),
            Stage.FINAL,
            STACK_NAME,
            branchInfo);

        SwaggerHubInfo newInfo = swaggerHubUpdater.getSwaggerHubInfo();
        assertThat(newInfo.getApiId(), is(equalTo(API_ID)));
    }


}
