package no.bibsys.aws.lambda.api.handlers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.UpdateStackRequest;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.testtutils.LocalTest;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.jupiter.api.Test;

public class UpdateStackRequestHandlerTest extends LocalTest {

    private static final String SOME_OWNER = "OWNER";
    private static final String SOME_REPO = "REPO";
    private static final String SOME_BRANCH = "BRAnCH";
    private static final String ARBITRARY_SECRET_VALUE = "secretValue";
    private static final Region ARBITRARY_REGION = Region.getRegion(Regions.EU_WEST_1);
    private final UpdateStackRequest request = new UpdateStackRequest(SOME_OWNER, SOME_REPO,
        SOME_BRANCH,
        "create");
    private final String requestJson;

    public UpdateStackRequestHandlerTest() throws JsonProcessingException {
        requestJson = JsonUtils.newJsonParser().writeValueAsString(request);
    }

    @Test
    public void handleRequest_falseSignature_UnauthorizedException() {
        UpdateStackRequestHandler handler = newHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", "wrongValue");

        assertThrows(UnauthorizedException.class,
            () -> handler.processInput(requestJson, headers, null));
    }

    private UpdateStackRequestHandler newHandlerWithMockSecretsReader() {
        Environment env = mockEnvironment(EnvironmentConstants.AWS_REGION,
            ARBITRARY_REGION.getName());
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler(
            env,
            mockCloudFormationClient(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient()
        );
        SecretsReader secretsReader = () -> ARBITRARY_SECRET_VALUE;
        handler.setSecretsReader(secretsReader);
        return handler;
    }
}
