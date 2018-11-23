package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.cloudformation.PipelineConfiguration;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.cloudformation.Stage;
import no.bibsys.cloudformation.helpers.ResourceType;
import no.bibsys.cloudformation.helpers.StackResources;
import no.bibsys.git.github.GitInfo;
import no.bibsys.lambda.deploy.handlers.Route53Updater;
import no.bibsys.lambda.deploy.handlers.SwaggerHubUpdater;
import no.bibsys.lambda.responses.SimpleResponse;

public class ResourceInitializer {

    private transient final SwaggerHubUpdater swaggerHubUpdater;
    private final transient Route53Updater route53Updater;
    private final transient String certificateArn;

    public ResourceInitializer(String zoneName, GitInfo gitInfo, String branch, Stage stage,
        String certificateArn) throws IOException {
        super();
        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(
            gitInfo, branch);

//        this.swaggerHubUpdater = new SwaggerHubUpdater(apiGateway, gitInfo.getRepo(), branch,
//            stage);
        this.route53Updater = new Route53Updater(zoneName, gitInfo.getRepo(), branch, stage,
            apiGateway);
        this.certificateArn = certificateArn;
    }


    private Optional<String> findRestApi(GitInfo gitInfo, String branch, Stage stage) {
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration(
            gitInfo.getRepo(), branch);
        String stackName = pipelineConfiguration.getCurrentServiceStackName(stage);
        StackResources stackResources = new StackResources(stackName);
        Optional<String> restApiId = stackResources
            .getResources(ResourceType.REST_API).stream()
            .map(resource -> resource.getPhysicalResourceId())
            .findAny();
        return restApiId;

    }

    public SimpleResponse initializeStacks()
        throws IOException, URISyntaxException {

        System.out.println("Lambda function started");
        System.out.println("Updating Route 53");

        deletePreviousResources();

        Optional<ChangeResourceRecordSetsResult> route53UpdateResult = route53Updater
            .updateServerUrl(certificateArn);
        String route53Status = route53UpdateResult.map(result -> result.getChangeInfo().getStatus())
            .orElse("Server not updated");

        StringBuilder output = new StringBuilder(20);
        output.append("Swagger:");

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
