package no.bibsys.aws.lambda.api.handlers;

import static org.mockito.Mockito.when;
import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.JsonUtils;
import no.bibsys.aws.lambda.api.requests.UpdateStackRequest;
import org.junit.Test;
import org.mockito.Mockito;

public class UpdateStackRequestHandlerTest {

    private final transient Environment environment = Mockito.mock(Environment.class);
    private final UpdateStackRequest request = new UpdateStackRequest("OWNER", "REPO", "BRAnCH", "create");
    private final String requestJson;

    public UpdateStackRequestHandlerTest() throws JsonProcessingException {
        requestJson = JsonUtils.newJsonParser().writeValueAsString(request);
    }

    @Test(expected = UnauthorizedException.class)
    public void handleRequest_falseSignature_UnauthorizedException() throws IOException, URISyntaxException {
        UpdateStackRequestHandler handler = newHandlerWithMockSecretsReader();
        Map<String, String> headers = new HashMap<>();
        headers.put("api-key", "wrongValue");
        handler.processInput(requestJson, headers, null);

    }


    private UpdateStackRequestHandler newHandlerWithMockSecretsReader() throws IOException {
        UpdateStackRequestHandler handler = new UpdateStackRequestHandler();
        SecretsReader secretsReader = Mockito.mock(SecretsReader.class);
        when(secretsReader.readSecret()).thenReturn("secretValue");
        handler.setSecretsReader(secretsReader);

        return handler;
    }

}
