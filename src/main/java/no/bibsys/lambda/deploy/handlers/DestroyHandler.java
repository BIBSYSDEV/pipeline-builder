package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;

public class DestroyHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {


    private final transient SwaggerHubUpdater swaggerHubUpdater;

    public DestroyHandler() throws IOException {
        super();

        swaggerHubUpdater = new SwaggerHubUpdater();
    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {

        Integer response = swaggerHubUpdater.deleteApi();

        System.out.println(response);
        return new SimpleResponse(response.toString());

    }


}
