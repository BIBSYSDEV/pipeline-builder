package no.bibsys.aws.lambda.api.handlers;

import static no.bibsys.aws.testtutils.LocalTest.APPROVE_ALL_KEYS;
import static no.bibsys.aws.testtutils.LocalTest.getGithubHandlerWithMockSecretsReader;
import static no.bibsys.aws.testtutils.LocalTest.mockEnvironment;
import static no.bibsys.aws.testtutils.LocalTest.mockSecretsReader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.lambda.api.requests.PullRequest;
import no.bibsys.aws.secrets.GithubSignatureChecker;
import no.bibsys.aws.testtutils.LocalStackWipingTest;
import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;

public class GithubHandlerTest extends LocalStackWipingTest {

    private static final String GITHUB_RESOURCES_FOLDER = "github";
    private static final String CLOSE_PULLREQUEST_JSON = "pullrequest.json";
    private static final String ARBIRTRARY_REQUEST = "{\"foo\":\"doo\"}";
    private static final String GITHUB_SIGNATURE_HEADER = "X-Hub-Signature";
    private static final String VALID_SIGNATURE_HEADER_VALUE = "sha1=9a56fd503f28caa0f65b7d341589ed7edb379024";

    @Test
    public void processInput_closePRrequest_actionClose() throws IOException {
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(
            mockSecretsReader(APPROVE_ALL_KEYS));
        GithubHandler githubHandler = new GithubHandler(mockEnvironment(),
            initializeMockCloudFormation(),
            initializeS3(), initializeLamdaClient(),
            initializeMockLogsClient(),
            signatureChecker);
        String githubCloseRequest = IoUtils.resourceAsString(Paths.get(GITHUB_RESOURCES_FOLDER,
            CLOSE_PULLREQUEST_JSON));

        String response = githubHandler.processInput(githubCloseRequest, new HashMap<>(), null);
        assertThat(response, is(equalTo(PullRequest.ACTION_CLOSE)));
    }

    @Test()
    public void handleRequest_falseSignature_UnauthorizedException() {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader(mockEnvironment());
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Hub-Signature", "sha1=586242134c853931b8df12ac69352f26e6d52453");
        assertThrows(UnauthorizedException.class,
            () -> githubHandler.processInput("something", headers, null));
    }

    @Test
    public void handleRequest_correctSignature_someMessage()
        throws IOException {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader(mockEnvironment());
        Map<String, String> headers = new HashMap<>();
        headers.put(GITHUB_SIGNATURE_HEADER, VALID_SIGNATURE_HEADER_VALUE);
        String result = githubHandler.processInput(ARBIRTRARY_REQUEST, headers, null);
        assertThat(result, is(equalTo("No action")));
    }
}
