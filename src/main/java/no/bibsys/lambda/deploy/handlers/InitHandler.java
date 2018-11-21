package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;
import no.bibsys.utils.Environment;
import no.bibsys.utils.ResourceInitializer;

public class InitHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {

    private final transient Environment  environment;


    public InitHandler() throws IOException {
        super();
        environment = new Environment();

    }



    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
        throws IOException, URISyntaxException {


        String zoneName= environment.readEnv(Route53Updater.ZONE_NAME_ENV);
        String repository= environment.readEnv(Route53Updater.REPOSITORY_NAME_ENV_VAR);
        String branch= environment.readEnv(Route53Updater.BRANCH_NAME_ENV_VAR);
        Stage stage=Stage.currentStage();
        String certificateArn= environment.readEnv(Route53Updater.CERTIFICATE_ARN);


        ResourceInitializer initializer=new ResourceInitializer(zoneName,repository,branch,
            new SwaggerHubInfo(environment),stage,certificateArn);
        initializer.initializeStacks();

        return new SimpleResponse("OK");

    }














}
