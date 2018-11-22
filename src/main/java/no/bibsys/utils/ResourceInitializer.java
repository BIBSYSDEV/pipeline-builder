package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.handlers.Route53Updater;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import no.bibsys.lambda.deploy.handlers.SwaggerHubUpdater;
import no.bibsys.lambda.responses.SimpleResponse;

public class ResourceInitializer {

    private transient final SwaggerHubUpdater swaggerHubUpdater;
    private final transient Route53Updater route53Updater;
    private final transient String certificateArn;

    public ResourceInitializer(String zoneName,String repository, String branch, SwaggerHubInfo swaggerHubInfo,
        Stage stage,String certificateArn) throws IOException {
        super();
        AmazonApiGateway apiGateway= AmazonApiGatewayClientBuilder.defaultClient();
        this.swaggerHubUpdater = new SwaggerHubUpdater(apiGateway,swaggerHubInfo,repository,branch,stage);
        this.route53Updater = new Route53Updater(zoneName, repository, branch, stage, apiGateway);
        this.certificateArn = certificateArn;
    }




    public SimpleResponse initializeStacks()
        throws IOException, URISyntaxException {

        System.out.println("Lambda function started");
        System.out.println("Updating Route 53");
        ChangeResourceRecordSetsResult route53UpdateResult = route53Updater
            .updateServerUrl(certificateArn);
        String route53Status = route53UpdateResult.getChangeInfo().getStatus();
        Route53Updater testPhaseRoute53Updater = route53Updater.copy(Stage.TEST);
        testPhaseRoute53Updater.deleteServerUrl();

        StringBuilder output = new StringBuilder(20);
        output.append("Swagger:");

        swaggerHubUpdater.deleteApi();
        Optional<String> swaggerUpdateResult = swaggerHubUpdater.updateApiDocumentation();
        swaggerUpdateResult.ifPresent(s -> output.append(s));
        output.append("\nRoute53:").append(route53Status);


        return new SimpleResponse(output.toString());

    }


}
