package no.bibsys.aws.lambda.api.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.lambda.api.requests.SimplePullRequest;
import no.bibsys.aws.secrets.GithubSignatureChecker;
import no.bibsys.aws.testtutils.LocalStackTest;
import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;

public class GithubHandlerTest extends LocalStackTest {

    private static final String GITHUB_RESOURCES_FOLDER = "github";
    private static final String CLOSE_PULLREQUEST_JSON = "github_close_pull_request_payload.json";
    private static final String OPEN_PULLREQUEST_JSON = "github_open_pull_request_payload.json";
    private static final String REOPEN_PULLREQUEST_JSON = "github_reopen_pull_request_payload.json";

    private static final String ARBIRTRARY_REQUEST = "{\"foo\":\"doo\"}";
    private static final String GITHUB_SIGNATURE_HEADER = "X-Hub-Signature";
    private static final String VALID_SIGNATURE_HEADER_VALUE = "sha1=9a56fd503f28caa0f65b7d341589ed7edb379024";
    private static final String FALSE_SIGNATURE = "sha1=586242134c853931b8df12ac69352f26e6d52453";
    private static final String ARBITRARY_REQUEST = "something";

    @Test
    public void processInput_closePRrequest_actionClose() throws Exception {
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(
            mockSecretsReader(APPROVE_ALL_KEYS));

        GithubHandler githubHandler = new GithubHandler(mockEnvironment(),
            mockCloudFormationWithStack(),
            mockS3Client(), mockLambdaClient(),
            mockLogsClient(),
            signatureChecker,
            mockSecretsReader(),
            mockSecretsReader(),
            mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole()),
            mockGithubReader()
        );
        String githubCloseRequest = IoUtils.resourceAsString(Paths.get(GITHUB_RESOURCES_FOLDER,
            CLOSE_PULLREQUEST_JSON));

        String response = githubHandler.processInput(githubCloseRequest, new HashMap<>(), null);
        assertThat(response, is(equalTo(SimplePullRequest.ACTION_CLOSE)));
    }

    @Test
    public void processInput_openPRrequest_actionOpen() throws Exception {
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(
            mockSecretsReader(APPROVE_ALL_KEYS));
        GithubHandler githubHandler = new GithubHandler(
            mockEnvironment(),
            mockCloudFormationWithStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            signatureChecker,
            mockSecretsReader(),
            mockSecretsReader(),
            mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole()),
            mockGithubReader()
        );
        String githubCloseRequest = IoUtils.resourceAsString(Paths.get(GITHUB_RESOURCES_FOLDER,
            OPEN_PULLREQUEST_JSON));

        String response = githubHandler.processInput(githubCloseRequest, new HashMap<>(), null);
        assertThat(response, is(equalTo(SimplePullRequest.ACTION_OPEN)));
    }

    @Test
    public void processInput_openPRrequest_actionReopen() throws Exception {
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(
            mockSecretsReader(APPROVE_ALL_KEYS));

        GithubHandler githubHandler = new GithubHandler(
            mockEnvironment(),
            mockCloudFormationWithStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            signatureChecker,
            mockSecretsReader(),
            mockSecretsReader(),
            mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole()),
            mockGithubReader()
        );
        String githubCloseRequest = IoUtils.resourceAsString(Paths.get(GITHUB_RESOURCES_FOLDER,
            REOPEN_PULLREQUEST_JSON));

        String response = githubHandler.processInput(githubCloseRequest, new HashMap<>(), null);
        assertThat(response, is(equalTo(SimplePullRequest.ACTION_REOPEN)));
    }

    @Test()
    public void handleRequest_falseSignature_UnauthorizedException() throws IOException {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader(mockEnvironment());
        Map<String, String> headers = new HashMap<>();
        headers.put(GITHUB_SIGNATURE_HEADER, FALSE_SIGNATURE);
        assertThrows(UnauthorizedException.class,
            () -> githubHandler.processInput(ARBITRARY_REQUEST, headers, null));
    }

    @Test
    public void handleRequest_correctSignature_someMessage()
            throws Exception {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader(mockEnvironment());
        Map<String, String> headers = new HashMap<>();
        headers.put(GITHUB_SIGNATURE_HEADER, VALID_SIGNATURE_HEADER_VALUE);
        String result = githubHandler.processInput(ARBIRTRARY_REQUEST, headers, null);
        assertThat(result, is(equalTo(GithubHandler.NO_ACTION_MESSAGE)));
    }
}
