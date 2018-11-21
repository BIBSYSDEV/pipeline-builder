package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.constants.NetworkConstants;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import no.bibsys.lambda.deploy.handlers.SwaggerHubUpdater;

public class ResourceDestroyer {

    private final transient SwaggerHubUpdater swaggerHubUpdater;
    private final transient ApiGatewayBasePathMapping apiGatewayBasePathMapping;

    public ResourceDestroyer(String repository,String branch, SwaggerHubInfo swaggerHubInfo,Stage stage)
        throws IOException {
        super();

        String domainName = new NetworkConstants(stage).getDomainName();
        AmazonApiGateway client = AmazonApiGatewayClientBuilder.defaultClient();
        swaggerHubUpdater = new SwaggerHubUpdater(client,swaggerHubInfo,repository,branch,stage);
        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        this.apiGatewayBasePathMapping = new ApiGatewayBasePathMapping(apiGateway,domainName,stage );
    }


    public  void destroy()
        throws IOException, URISyntaxException {

        int response = swaggerHubUpdater.deleteApi();
        apiGatewayBasePathMapping.deleteBasePathMappings();

        System.out.println("Swagger response" + response);


    }






}
