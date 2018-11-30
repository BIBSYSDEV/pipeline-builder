package no.bibsys.aws.lambda.deploy.handlers;

import static no.bibsys.aws.lambda.EnvironmentConstants.CERTIFICATE_ARN;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.lambda.events.DeployEvent;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.resources.ResourceInitializer;

public class InitHandler extends ResourceHandler{


    public InitHandler() throws IOException {
        super(new Environment());
    }



    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
            throws IOException, URISyntaxException {


        String certificateArn = environment.readEnv(CERTIFICATE_ARN);



        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(swagerApiId,swagerApiVersion,swagerApiOwner);


        ResourceInitializer initializer =
                new ResourceInitializer(zoneName, applicationUrl,stackName, swaggerHubInfo, stage, certificateArn);
        initializer.initializeStacks();

        return new SimpleResponse("OK");

    }



}
