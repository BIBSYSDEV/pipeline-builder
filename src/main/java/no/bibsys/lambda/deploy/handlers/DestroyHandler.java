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

    private final transient Environment environment;

    public DestroyHandler(Environment environment)  {
        super();
        this.environment=environment;

    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {
        Stage stage = Stage.currentStage();
        String repository=environment.readEnv("REPOSITORY");
        String branch=environment.readEnv("BRANCH");
        SwaggerHubInfo swaggerHubInfo=new SwaggerHubInfo(environment);
        ResourceDestroyer resourceDestroyer=new ResourceDestroyer(repository,branch,swaggerHubInfo,stage);
        resourceDestroyer.destroy();

        return new SimpleResponse("OK");

    }


}
