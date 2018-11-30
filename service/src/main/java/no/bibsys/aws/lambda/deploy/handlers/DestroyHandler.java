package no.bibsys.aws.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.lambda.events.DeployEvent;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.resources.ResourceDestroyer;

public class DestroyHandler extends ResourceHandler {

    public DestroyHandler() {
        this(new Environment());
    }


    public DestroyHandler(Environment environment) {
        super(environment);
    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {




        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(swagerApiId,swagerApiVersion,swagerApiOwner);

        ResourceDestroyer resourceDestroyer = new ResourceDestroyer(zoneName, applicationUrl,
            stackName, swaggerHubInfo, stage);
        resourceDestroyer.destroy();

        return new SimpleResponse("OK");

    }


}
