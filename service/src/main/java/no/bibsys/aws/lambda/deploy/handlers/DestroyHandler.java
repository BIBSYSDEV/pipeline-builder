package no.bibsys.aws.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.aws.lambda.deploy.requests.DeployEvent;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.route53.Route53Updater;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.resources.ResourceDestroyer;

public class DestroyHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {

    private final transient Environment environment;

    public DestroyHandler() {
        this(new Environment());
    }


    public DestroyHandler(Environment environment) {
        super();
        this.environment = environment;

    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString, Context context)
            throws IOException, URISyntaxException {
        Stage stage = Stage.currentStage();
        String zoneName = environment.readEnv(Route53Updater.ZONE_NAME_ENV);

        GitInfo gitInfo = new GithubConf(environment);
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(environment);

        ResourceDestroyer resourceDestroyer = new ResourceDestroyer(zoneName, gitInfo, swaggerHubInfo, stage);
        resourceDestroyer.destroy();

        return new SimpleResponse("OK");

    }


}
