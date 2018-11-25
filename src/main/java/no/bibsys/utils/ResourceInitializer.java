package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.cloudformation.Stage;
import no.bibsys.git.github.GitInfo;
import no.bibsys.lambda.deploy.handlers.Route53Updater;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import no.bibsys.lambda.deploy.handlers.SwaggerHubUpdater;
import no.bibsys.lambda.responses.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * It is called after the creation of a CloudFormation Stack in order to create resources that do
 * not belong to the Stack. These resources can be inside or outside AWS. It is usually called
 * thought a handler of a Lambda function (see {@link no.bibsys.lambda.deploy.handlers.InitHandler}).
 *
 *
 * Currently it stores the API specification to SwaggerHub and creates all Route53 and ApiGateway
 * configurations related to attaching the branch's RestApi to a static url.
 */
public class ResourceInitializer extends ResourceManager {

    private final Logger logger = LoggerFactory.getLogger(ResourceInitializer.class);

    private transient final SwaggerHubUpdater swaggerHubUpdater;
    private final transient Route53Updater route53Updater;
    private final transient String certificateArn;


    public ResourceInitializer(String zoneName,
        GitInfo gitInfo,
        SwaggerHubInfo swaggerHubInfo,
        Stage stage,
        String certificateArn) throws IOException {
        super();
        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        String apiGatewayRestApi = findRestApi(gitInfo, stage);

        this.swaggerHubUpdater = new SwaggerHubUpdater(apiGateway, apiGatewayRestApi,
            swaggerHubInfo,
            stage);
        this.route53Updater = new Route53Updater(zoneName, gitInfo, stage, apiGatewayRestApi,
            apiGateway);
        this.certificateArn = certificateArn;
    }


    public SimpleResponse initializeStacks()
        throws IOException, URISyntaxException {

        if (logger.isInfoEnabled()) {
            logger.info("Lambda function started");
            logger.info("Updating Route 53");
        }


        deletePreviousResources();

        Optional<ChangeResourceRecordSetsResult> route53UpdateResult = route53Updater
            .updateServerUrl(certificateArn);
        String route53Status = route53UpdateResult.map(result -> result.getChangeInfo().getStatus())
            .orElse("Server not updated");

        StringBuilder output = new StringBuilder(20);
        output.append("Swagger:");

        if (logger.isInfoEnabled()) {
            logger.info("SwaggerUpdate started");
        }
        Optional<String> swaggerUpdateResult = swaggerHubUpdater.updateApiDocumentation();
        swaggerUpdateResult.ifPresent(s -> output.append(s));
        output.append("\nRoute53:").append(route53Status);

        return new SimpleResponse(output.toString());

    }

    private void deletePreviousResources() throws URISyntaxException, IOException {
        Route53Updater testPhaseRoute53Updater = route53Updater.copy(Stage.TEST);
        testPhaseRoute53Updater.deleteServerUrl();
        swaggerHubUpdater.deleteApi();
    }


}
