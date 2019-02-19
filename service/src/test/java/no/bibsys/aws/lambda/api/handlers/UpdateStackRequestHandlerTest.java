package no.bibsys.aws.lambda.api.handlers;

import static no.bibsys.aws.testtutils.LocalTest.mockCloudFormationClient;
import static no.bibsys.aws.testtutils.LocalTest.mockEnvironment;
import static no.bibsys.aws.testtutils.LocalTest.mockLambdaClient;
import static no.bibsys.aws.testtutils.LocalTest.mockLogsClient;
import static no.bibsys.aws.testtutils.LocalTest.mockS3Client;
import static no.bibsys.aws.testtutils.LocalTest.mockSecretsReader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.UpdateStackRequest;
import no.bibsys.aws.lambda.api.utils.Action;
import no.bibsys.aws.testtutils.LocalStackWipingTest;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.jupiter.api.Test;

public class UpdateStackRequestHandlerTest extends LocalStackWipingTest {

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
    public void processInput_closePRrequest_actionClose() throws IOException {
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler(mockEnvironment(),
            initializeMockCloudFormation(),
            initializeS3(), initializeLamdaClient(),
            initializeMockLogsClient(),
            mockSecretsReader(),
            mockSecretsReader()
        );
        String json = deleteStackRequest();
        String key = mockSecretsReader().readSecret();
        Map<String, String> headersMap = Collections.singletonMap(
            UpdateStackRequestHandler.API_KEY_HEADER, key);

        String response = handler.processInput(json, headersMap, null);
        assertThat(response, is(equalTo(json)));
    }

    @Test
    public void processInput_createStackRequest_actionCreate() throws IOException {
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler(mockEnvironment(),
            initializeMockCloudFormation(),
            initializeS3(), initializeLamdaClient(),
            initializeMockLogsClient(),
            mockSecretsReader(),
            mockSecretsReader()
        );
        String json = createStackRequest();
        String key = mockSecretsReader().readSecret();
        Map<String, String> headersMap = Collections.singletonMap(
            UpdateStackRequestHandler.API_KEY_HEADER, key);

        String response = handler.processInput(json, headersMap, null);
        assertThat(response, is(equalTo(json)));
    }

    private String deleteStackRequest() throws JsonProcessingException {
        UpdateStackRequest request = new UpdateStackRequest(SOME_OWNER, SOME_REPO, SOME_BRANCH,
            Action.DELETE);
        ObjectMapper parser = JsonUtils.newJsonParser();
        return parser.writeValueAsString(request);
    }

    private String createStackRequest() throws JsonProcessingException {
        UpdateStackRequest request = new UpdateStackRequest(SOME_OWNER, SOME_REPO, SOME_BRANCH,
            Action.CREATE);
        ObjectMapper parser = JsonUtils.newJsonParser();
        return parser.writeValueAsString(request);
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
            mockLogsClient(),
            mockSecretsReader(),
            mockSecretsReader()
        );

        return handler;
    }
}
