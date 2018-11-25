package no.bibsys.lambda.api.handlers;


import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import no.bibsys.lambda.api.requests.UpdateStackRequest;
import no.bibsys.lambda.api.utils.Action;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;
import no.bibsys.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateStackRequestHandler extends GithubHandler {


    private static final Logger logger = LoggerFactory.getLogger(UpdateStackRequestHandler.class);

    private static final String AWS_SECRET_NAME = "infrastructure";
    private static final String AWS_SECRET_KEY = "buildbranch";


    private transient SecretsReader secretsReader;
    private static final String API_KEY_HEADER = "api-key";


    public UpdateStackRequestHandler(Environment environment) {
        super(environment);
        this.secretsReader = new SecretsReader(AWS_SECRET_NAME, AWS_SECRET_KEY);
    }


    public UpdateStackRequestHandler() {
        this(new Environment());
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

        if (logger.isInfoEnabled()) {
            logger.info(request.toString());
        }

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


