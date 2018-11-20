package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.constants.NetworkConstants;
import no.bibsys.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;
import no.bibsys.utils.Environment;

public class DestroyHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {


    private final transient SwaggerHubUpdater swaggerHubUpdater;
    private final transient ApiGatewayBasePathMapping apiGatewayBasePathMapping;

    public DestroyHandler() throws IOException {
        super();
        Environment envinoment=new Environment();
        Stage stage = initStage(envinoment);
        String domainName=initDomainName(stage);
        AmazonApiGateway client= AmazonApiGatewayClientBuilder.defaultClient();
        swaggerHubUpdater = new SwaggerHubUpdater(client);

        apiGatewayBasePathMapping = new ApiGatewayBasePathMapping(client, domainName,stage);
    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {

        Integer response = swaggerHubUpdater.deleteApi();
        apiGatewayBasePathMapping.deleteBasePathMappings();

        System.out.println("Swagger reponse"+response);
        return new SimpleResponse("OK");

    }

    private Stage initStage(Environment env){
        String stage=env.readEnv("STAGE");
        return Stage.fromString(stage);
    }


    private String  initDomainName(Stage stage){
        if(!stage.equals(Stage.FINAL)){
           return "test."+ NetworkConstants.DOMAIN_NAME;
        }
        else{
            return NetworkConstants.DOMAIN_NAME;
        }

    }

}
