package no.bibsys.lambda.api.handlers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.lambda.api.utils.SignatureChecker;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;
import org.junit.Test;
import org.mockito.Mockito;

public class GithubHandlerTest {


    Environment environment = Mockito.mock(Environment.class);

    @Test(expected = UnauthorizedException.class)
    public void handleRequest_falseSignature_UnauthorizedException()
        throws IOException, URISyntaxException {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Hub-Signature", "sha1=586242134c853931b8df12ac69352f26e6d52453");
        githubHandler.processInput("something", headers, null);

    }


    @Test
    public void handleRequest_correctSignature_someMessage()
        throws IOException, URISyntaxException {
        GithubHandler githubHandler = getGithubHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Hub-Signature", "sha1=d7e0fa26e7ae74b65a8e0e1b6c977a31da562d02");
        String result = githubHandler.processInput("{\"foo\":\"doo\"}", headers, null);
        assertThat(result, is(equalTo("No action")));

    }


    private GithubHandler getGithubHandlerWithMockSecretsReader() throws IOException {
        GithubHandler githubHandler = new GithubHandler(environment);
        SignatureChecker signatureChecker = new SignatureChecker("secreteName", "secretKey");
        SecretsReader secretsReader = Mockito.mock(SecretsReader.class);
        signatureChecker.setSecretsReader(secretsReader);
        when(secretsReader.readSecret()).thenReturn("secretValue");
        githubHandler.setSignatureChecker(signatureChecker);
        return githubHandler;
    }

}
