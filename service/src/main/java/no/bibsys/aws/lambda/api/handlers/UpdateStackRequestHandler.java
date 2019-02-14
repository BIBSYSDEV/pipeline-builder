package no.bibsys.aws.lambda.api.handlers;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.UpdateStackRequest;
import no.bibsys.aws.lambda.api.utils.Action;
import no.bibsys.aws.secrets.AWSSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStackRequestHandler extends ApiHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateStackRequest.class);
    private static final String API_KEY_HEADER = "api-key";
    private transient SecretsReader secretsReader;

    public UpdateStackRequestHandler(Environment environment) {
        super(environment);
        String secretName = environment.readEnv(EnvironmentConstants.REST_USER_API_KEY_SECRET_NAME);
        String secretKey = environment.readEnv(EnvironmentConstants.REST_USER_API_KEY_SECRET_KEY);
        this.secretsReader = new AWSSecretsReader(secretName, secretKey,region);
    }

    @Override
    public String processInput(String string, Map<String, String> headers, Context context)
        throws IOException {

        String securityToken = headers.get(API_KEY_HEADER);
        checkAuthorization(securityToken);
        UpdateStackRequest request = parseRequest(string);

        if (request.getAction().equals(Action.CREATE)) {
            createStacks(request);
        }

        if (request.getAction().equals(Action.DELETE)) {
            deleteStacks(request);
        }

        logger.debug(request.toString());

        ObjectMapper objectMapper = JsonUtils.newJsonParser();
        String requestJson = objectMapper.writeValueAsString(request);
        return requestJson;
    }

    private UpdateStackRequest parseRequest(String string) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        return mapper.readValue(string, UpdateStackRequest.class);
    }

    private void checkAuthorization(String securityToken) throws IOException {

        String secret = secretsReader.readSecret();
        if (!secret.equals(securityToken)) {
            throw new UnauthorizedException("Wrong API key signature");
        }
    }

    public void setSecretsReader(SecretsReader secretsReader) {
        this.secretsReader = secretsReader;
    }
}


