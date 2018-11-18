package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;

public class InitHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {


    private transient final SwaggerHubUpdater swaggerHubUpdater;
    private final transient Route53Updater route53Updater;

    public InitHandler() throws IOException {
        super();
        this.swaggerHubUpdater = new SwaggerHubUpdater();
        this.route53Updater = new Route53Updater();
    }

    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
        throws IOException, URISyntaxException {

        System.out.println("Lambda function started");

        Optional<String> swaggerUpdateResult = swaggerHubUpdater.updateApiDocumentation();
        Optional<ChangeResourceRecordSetsResult> route53UpdateResult = route53Updater
            .updateServerUrl();
        Optional<String> route53Status = route53UpdateResult
            .map(result -> result.getChangeInfo().getStatus());
        StringBuilder output = new StringBuilder();
        output.append("Swagger:");
        swaggerUpdateResult.ifPresent(s -> output.append(s));
        output.append("\nRoute53:");
        route53Status.ifPresent(s -> output.append(s));

        return new SimpleResponse(output.toString());

    }












}
