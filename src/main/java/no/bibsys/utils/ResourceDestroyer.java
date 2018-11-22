package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.handlers.Route53Updater;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import no.bibsys.lambda.deploy.handlers.SwaggerHubUpdater;

public class ResourceDestroyer {

    private final transient SwaggerHubUpdater swaggerHubUpdater;
    private final transient Route53Updater route53Updater;

    public ResourceDestroyer(String zoneName, String repository, String branch,
        SwaggerHubInfo swaggerHubInfo, Stage stage)
        throws IOException {
        super();
        AmazonApiGateway client = AmazonApiGatewayClientBuilder.defaultClient();
        swaggerHubUpdater = new SwaggerHubUpdater(client,swaggerHubInfo,repository,branch,stage);
        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        route53Updater = new Route53Updater(zoneName, repository, branch, stage, apiGateway);
    }


    public  void destroy()
        throws IOException, URISyntaxException {

        int response = swaggerHubUpdater.deleteApi();

        this.route53Updater.deleteServerUrl();


        System.out.println("Swagger response" + response);


    }






}
