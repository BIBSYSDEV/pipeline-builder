package no.bibsys.lambda.api.handlers;


import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import no.bibsys.lambda.api.requests.UpdateStackRequest;
import no.bibsys.lambda.api.utils.Action;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.JsonUtils;

public class UpdateStackRequestHandler extends GithubHandler {


    private final transient SecretsReader secretsReader;

    public UpdateStackRequestHandler(){
        super();
        this.secretsReader=new SecretsReader();
    }


    @Override
    public String processInput(String string, Map<String, String> headers, Context context)
        throws IOException, URISyntaxException {

        ObjectMapper mapper = JsonUtils.newJsonParser();
        UpdateStackRequest request = mapper.readValue(string, UpdateStackRequest.class);
        String securityToken = headers.get("api-key");
        checkAuthorization(securityToken);
        if (request.getAction().equals(Action.CREATE)) {
            createStacks(request);
        }

        if (request.getAction().equals(Action.DELETE)) {
            deleteStacks(request);
        }

        System.out.println(request.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        return requestJson;

    }

    private void checkAuthorization(String securityToken) throws IOException {

        String secret=secretsReader.readAuthFromSecrets("infrastructure","buildbranch");
        if(!secret.equals(securityToken)){
             throw new UnauthorizedException("Wrong API key signature");
        }
    }


}


