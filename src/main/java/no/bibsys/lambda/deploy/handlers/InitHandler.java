package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;
import no.bibsys.utils.Environment;

public class InitHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {


    private transient final SwaggerHubUpdater swaggerHubUpdater;
    private final transient Route53Updater route53Updater;

    public InitHandler() throws IOException {
        super();
        AmazonApiGateway apiGateway=AmazonApiGatewayClientBuilder.defaultClient();
        this.swaggerHubUpdater = new SwaggerHubUpdater(apiGateway);
        Environment environment = new Environment();
        String zoneName= environment.readEnv(Route53Updater.ZONE_NAME_ENV);
        String repository= environment.readEnv(Route53Updater.REPOSITORY_NAME_ENV_VAR);
        String branch= environment.readEnv(Route53Updater.BRANCH_NAME_ENV_VAR);
        Stage stage=Stage.fromString(environment.readEnv(Route53Updater.BRANCH_NAME_ENV_VAR));
        String certificateArn= environment.readEnv(Route53Updater.CERTIFICATE_ARN);

        this.route53Updater = new Route53Updater(zoneName,repository,branch,stage,certificateArn,apiGateway);
    }



    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
        throws IOException, URISyntaxException {

        System.out.println("Lambda function started");
        System.out.println("Updating Route 53");
        Optional<ChangeResourceRecordSetsResult> route53UpdateResult = route53Updater
            .updateServerUrl();
        Optional<String> route53Status = route53UpdateResult
            .map(result -> result.getChangeInfo().getStatus());
        StringBuilder output = new StringBuilder(20);
        output.append("Swagger:");
        Optional<String> swaggerUpdateResult = swaggerHubUpdater.updateApiDocumentation();
        swaggerUpdateResult.ifPresent(s -> output.append(s));
        output.append("\nRoute53:");
        route53Status.ifPresent(s -> output.append(s));

        return new SimpleResponse(output.toString());

    }














}
