package no.bibsys.aws.lambda.api.handlers;

import static no.bibsys.aws.testtutils.LocalTest.mockEnvironment;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.lambda.api.requests.UpdateStackRequest;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UpdateStackRequestHandlerTest {

    private final UpdateStackRequest request = new UpdateStackRequest("OWNER", "REPO", "BRAnCH",
        "create");
    private final String requestJson;

    public UpdateStackRequestHandlerTest() throws JsonProcessingException {
        requestJson = JsonUtils.newJsonParser().writeValueAsString(request);
    }

    @Test
    public void handleRequest_falseSignature_UnauthorizedException()
        throws IOException {
        UpdateStackRequestHandler handler = newHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", "wrongValue");

        assertThrows(UnauthorizedException.class,
            () -> handler.processInput(requestJson, headers, null));
    }

    private UpdateStackRequestHandler newHandlerWithMockSecretsReader() throws IOException {
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler(mockEnvironment());
        SecretsReader secretsReader = Mockito.mock(SecretsReader.class);
        when(secretsReader.readSecret()).thenReturn("secretValue");
        handler.setSecretsReader(secretsReader);

        return handler;
    }

}
