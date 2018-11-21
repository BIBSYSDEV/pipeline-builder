package no.bibsys.lambda.api.handlers;


import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import no.bibsys.lambda.api.requests.UpdateStackRequest;
import no.bibsys.lambda.api.utils.Action;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;
import no.bibsys.utils.JsonUtils;

public class UpdateStackRequestHandler extends GithubHandler {


    private transient SecretsReader secretsReader;
    private final transient SwaggerHubInfo swaggerHubInfo;
    private static final String API_KEY_HEADER="api-key";

    public UpdateStackRequestHandler(Environment environment){
        super(environment);
        this.secretsReader = new SecretsReader("infrastructure", "buildbranch");
        this.swaggerHubInfo=new SwaggerHubInfo(environment);
    }


    public UpdateStackRequestHandler(){
        this(new Environment());
    }


    @Override
    public String processInput(String string, Map<String, String> headers, Context context)
        throws IOException, URISyntaxException {

        String securityToken = headers.get(API_KEY_HEADER);
        checkAuthorization(securityToken);
        UpdateStackRequest request = parseRequest(string);

        if (request.getAction().equals(Action.CREATE)) {
            createStacks(request,swaggerHubInfo);
        }

        if (request.getAction().equals(Action.DELETE)) {
            deleteStacks(request,swaggerHubInfo);
        }

        System.out.println(request.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        return requestJson;

    }

    private UpdateStackRequest parseRequest(String string) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        return mapper.readValue(string, UpdateStackRequest.class);
    }

    private void checkAuthorization(String securityToken) throws IOException {

        String secret = secretsReader.readSecret();
        if(!secret.equals(securityToken)){
             throw new UnauthorizedException("Wrong API key signature");
        }
    }

    public void setSecretsReader(SecretsReader secretsReader) {
        this.secretsReader = secretsReader;
    }



}


