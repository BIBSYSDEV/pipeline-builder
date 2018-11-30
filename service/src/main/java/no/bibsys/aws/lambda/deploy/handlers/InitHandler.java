package no.bibsys.aws.lambda.deploy.handlers;

import static no.bibsys.aws.lambda.EnvironmentConstants.APPLICATION_URL;
import static no.bibsys.aws.lambda.EnvironmentConstants.CERTIFICATE_ARN;
import static no.bibsys.aws.lambda.EnvironmentConstants.STACK_NAME;
import static no.bibsys.aws.lambda.EnvironmentConstants.ZONE_NAME_ENV;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.lambda.events.DeployEvent;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.resources.ResourceInitializer;

public class InitHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {

    private final transient Environment environment;







    public InitHandler() throws IOException {
        super();
        environment = new Environment();

    }



    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
            throws IOException, URISyntaxException {

        String zoneName = environment.readEnv(ZONE_NAME_ENV);
        String stackName=environment.readEnv(STACK_NAME);
        Stage stage = Stage.currentStage();
        String certificateArn = environment.readEnv(CERTIFICATE_ARN);
        String applicationUrl=environment.readEnv(APPLICATION_URL);

        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(environment);

        ResourceInitializer initializer =
                new ResourceInitializer(zoneName, applicationUrl,stackName, swaggerHubInfo, stage, certificateArn);
        initializer.initializeStacks();

        return new SimpleResponse("OK");

    }



}
