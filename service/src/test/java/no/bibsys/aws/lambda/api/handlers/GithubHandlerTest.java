package no.bibsys.aws.lambda.api.handlers;

import static no.bibsys.aws.lambda.EnvironmentConstants.AWS_REGION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.secrets.GithubSignatureChecker;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.testtutils.LocalTest;
import no.bibsys.aws.tools.Environment;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GithubHandlerTest extends LocalTest {

    private final transient Environment environment;

    public GithubHandlerTest() {
        environment = mockEnvironment(AWS_REGION, ARBITRARY_REGION.getName());
    }

    @Test()
    public void handleRequest_falseSignature_UnauthorizedException()
        throws IOException {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Hub-Signature", "sha1=586242134c853931b8df12ac69352f26e6d52453");
        assertThrows(UnauthorizedException.class,
            () -> githubHandler.processInput("something", headers, null));
    }

    private GithubHandler getGithubHandlerWithMockSecretsReader() throws IOException {
        GithubHandler githubHandler = new GithubHandler(environment, mockCloudFormationClient(),
            mockS3Client(), mockLambdaClient(), mockLogsClient());
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(mockSecretsReader());
        SecretsReader secretsReader = Mockito.mock(SecretsReader.class);
        signatureChecker.setSecretsReader(secretsReader);
        when(secretsReader.readSecret()).thenReturn("secretValue");
        githubHandler.setSignatureChecker(signatureChecker);
        return githubHandler;
    }

    @Test
    public void handleRequest_correctSignature_someMessage()
        throws IOException {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Hub-Signature", "sha1=d7e0fa26e7ae74b65a8e0e1b6c977a31da562d02");
        String result = githubHandler.processInput("{\"foo\":\"doo\"}", headers, null);
        assertThat(result, is(equalTo("No action")));
    }
}
