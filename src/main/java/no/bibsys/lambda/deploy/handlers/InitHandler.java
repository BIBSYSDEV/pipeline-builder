package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;

public class InitHandler extends SwaggerHubUpdater {


    public InitHandler() throws IOException {
        super();
    }

    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
        throws IOException, URISyntaxException {

        System.out.println("Lambda function started");

        updateApiDocumentation();

        return new SimpleResponse("OK");

    }












}
