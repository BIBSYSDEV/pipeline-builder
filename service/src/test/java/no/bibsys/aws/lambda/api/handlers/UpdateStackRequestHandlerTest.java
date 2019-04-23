package no.bibsys.aws.lambda.api.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.UpdateStackRequest;
import no.bibsys.aws.lambda.api.utils.Action;
import no.bibsys.aws.roles.CreateStackRole;
import no.bibsys.aws.roles.CreateStackRoleImpl;
import no.bibsys.aws.testtutils.LocalStackTest;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.JsonUtils;
import no.bibsys.aws.utils.github.GithubReader;
import no.bibsys.aws.utils.github.GithubRestReader;
import org.junit.jupiter.api.Test;

public class UpdateStackRequestHandlerTest extends LocalStackTest {

    private static final String SOME_OWNER = "OWNER";
    private static final String SOME_REPO = "REPO";
    private static final String SOME_BRANCH = "BRAnCH";
    private static final Region ARBITRARY_REGION = Region.getRegion(Regions.EU_WEST_1);
    private static final String API_KEY_HEADEER = "api-key";
    private static final String WRONG_API_KEY = "wrongValue";
    private final UpdateStackRequest request = new UpdateStackRequest(SOME_OWNER, SOME_REPO,
        SOME_BRANCH,
        "create");
    private final String requestJson;

    public UpdateStackRequestHandlerTest() throws JsonProcessingException {
        requestJson = JsonUtils.newJsonParser().writeValueAsString(request);
    }

    @Test
    public void processInput_closeRequest_actionClose() throws Exception {
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler(mockEnvironment(),
            initializeMockCloudFormation(),
            initializeS3(), initializeLambdaClient(),
            initializeMockLogsClient(),
            mockSecretsReader(),
            mockSecretsReader(),
            mockIdentityManagement(pipelineStackConfiguration)
        );
        String json = deleteStackRequest();
        String key = mockSecretsReader().readSecret();
        Map<String, String> headersMap = Collections.singletonMap(
            UpdateStackRequestHandler.API_KEY_HEADER, key);

        String response = handler.processInput(json, headersMap, null);
        assertThat(response, is(equalTo(json)));
    }

    @Test
    public void processInput_createStackRequest_actionCreate() throws Exception {
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler(mockEnvironment(),
            initializeMockCloudFormation(),
            initializeS3(), initializeLambdaClient(),
            initializeMockLogsClient(),
            mockSecretsReader(),
            mockSecretsReader(),
            mockIdentityManagement(pipelineStackConfiguration)
        );

        CreateStackRole createStackRole = new CreateStackRoleImpl(
                mockGithubReader(),
                pipelineStackConfiguration,
                mockIdentityManagement(pipelineStackConfiguration));

        when(createStackRole.createNewCreateRoleRequest()).thenReturn(new CreateRoleRequest());

        String json = createStackRequest();
        String key = mockSecretsReader().readSecret();
        Map<String, String> headersMap = Collections.singletonMap(
            UpdateStackRequestHandler.API_KEY_HEADER, key);

        String response = handler.processInput(json, headersMap, null);
        assertThat(response, is(equalTo(json)));
    }

    private String deleteStackRequest() throws JsonProcessingException {
        UpdateStackRequest request = new UpdateStackRequest(SOME_OWNER, SOME_REPO, SOME_BRANCH,
            Action.DELETE.toString());
        ObjectMapper parser = JsonUtils.jsonParser;
        return parser.writeValueAsString(request);
    }

    private String createStackRequest() throws JsonProcessingException {
        UpdateStackRequest request = new UpdateStackRequest(SOME_OWNER, SOME_REPO, SOME_BRANCH,
            Action.CREATE.toString());
        ObjectMapper parser = JsonUtils.jsonParser;
        return parser.writeValueAsString(request);
    }

    @Test
    public void handleRequest_falseSignature_UnauthorizedException() {
        UpdateStackRequestHandler handler = newHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put(API_KEY_HEADEER, WRONG_API_KEY);

        assertThrows(UnauthorizedException.class,
            () -> handler.processInput(requestJson, headers, null));
    }

    private UpdateStackRequestHandler newHandlerWithMockSecretsReader() {
        Environment env = mockEnvironment(EnvironmentConstants.AWS_REGION,
            ARBITRARY_REGION.getName());
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler(
            env,
            initializeMockCloudFormation(),
            initializeS3(),
            initializeLambdaClient(),
            initializeMockLogsClient(),
            mockSecretsReader(),
            mockSecretsReader(),
            mockIdentityManagement(pipelineStackConfiguration)
        );

        return handler;
    }
}
