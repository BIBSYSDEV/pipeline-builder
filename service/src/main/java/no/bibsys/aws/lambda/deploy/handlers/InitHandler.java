package no.bibsys.aws.lambda.deploy.handlers;

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

    /**
     * Environment variable for reading the ROUTE 53 Hosted Zone name.
     */
    public static final String ZONE_NAME_ENV = "ZONE_NAME";

    /**
     * ARN of a regional certificate stored in the AWS Certficate Manager.
     */
    public static final String CERTIFICATE_ARN = "REGIONAL_CERTIFICATE_ARN";


    public static final String STACK_ID="STACK_ID";



    public InitHandler() throws IOException {
        super();
        environment = new Environment();

    }



    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
            throws IOException, URISyntaxException {

        String zoneName = environment.readEnv(ZONE_NAME_ENV);
        String stackId=environment.readEnv(STACK_ID);
        Stage stage = Stage.currentStage();
        String certificateArn = environment.readEnv(CERTIFICATE_ARN);

        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(environment);
        ResourceInitializer initializer =
                new ResourceInitializer(zoneName, stackId, swaggerHubInfo, stage, certificateArn);
        initializer.initializeStacks();

        return new SimpleResponse("OK");

    }



}
