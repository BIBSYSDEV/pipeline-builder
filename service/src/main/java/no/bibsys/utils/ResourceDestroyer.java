package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.lambda.deploy.handlers.Route53Updater;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.lambda.deploy.handlers.SwaggerHubUpdater;


/**
 * Opposite functionality of the ResourceInitializer. It is called before the deletion of a
 * CloudFormation Stack in order to disconnect the Stack with resources that do not belong to the
 * stack (either inside or outside AWS). It also deletes the disconnected resources. It is usually
 * called thought a handler of a Lambda function (see {@link no.bibsys.lambda.deploy.handlers.DestroyHandler}).
 *
 *
 *
 * Currently it deletes the API from SwaggerHub and all Route53 and ApiGateway configurations
 * related to attaching the branch's RestApi to a static url.
 */

public class ResourceDestroyer extends ResourceManager {

    private final transient SwaggerHubUpdater swaggerHubUpdater;
    private final transient Route53Updater route53Updater;

    public ResourceDestroyer(String zoneName, GitInfo gitInfo, SwaggerHubInfo swaggerHubInfo,
        Stage stage)
        throws IOException {
        super();
        AmazonApiGateway client = AmazonApiGatewayClientBuilder.defaultClient();

        String apiGatewayRestApiId = findRestApi(gitInfo, stage);
        swaggerHubUpdater = new SwaggerHubUpdater(client, apiGatewayRestApiId, swaggerHubInfo,
            stage);
        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        route53Updater = new Route53Updater(zoneName, gitInfo, stage, apiGatewayRestApiId,
            apiGateway);
    }


    public  void destroy()
        throws IOException, URISyntaxException {

        int response = swaggerHubUpdater.deleteApi();

        this.route53Updater.deleteServerUrl();

        System.out.println("Swagger response" + response);


    }


}
