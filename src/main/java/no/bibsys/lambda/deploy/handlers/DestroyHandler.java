package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;
import no.bibsys.utils.Environment;
import no.bibsys.utils.ResourceDestroyer;

public class DestroyHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {

    Environment environment;

    public DestroyHandler(Environment environment) throws IOException {
        super();
        this.environment=environment;

    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {
        Stage stage = Stage.fromString(environment.readEnv(Route53Updater.STAGE_ENV));
        ResourceDestroyer resourceDestroyer=new ResourceDestroyer(stage);
        resourceDestroyer.destroy();

        return new SimpleResponse("OK");

    }


}
