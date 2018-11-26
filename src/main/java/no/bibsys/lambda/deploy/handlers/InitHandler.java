package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.cloudformation.Stage;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubConf;
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
        String repoOwner = environment.readEnv(GithubConf.REPO_OWNER);
        String repository = environment.readEnv(GithubConf.REPOSITORY);
        String branch = environment.readEnv(GithubConf.BRANCH);
        Stage stage=Stage.currentStage();
        String certificateArn= environment.readEnv(Route53Updater.CERTIFICATE_ARN);

        GitInfo gitInfo = new GithubConf(repoOwner, repository, branch);
        SwaggerHubInfo swaggerHubInfo = new SwaggerHubInfo(environment);
        ResourceInitializer initializer = new ResourceInitializer(zoneName, gitInfo, swaggerHubInfo,
            stage, certificateArn);
        initializer.initializeStacks();

        return new SimpleResponse("OK");

    }














}
