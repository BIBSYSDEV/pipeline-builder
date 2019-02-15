package no.bibsys.aws.lambda.deploy.handlers;

import static no.bibsys.aws.lambda.EnvironmentConstants.CERTIFICATE_ARN;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.aws.git.github.BranchInfo;
import no.bibsys.aws.lambda.events.DeployEvent;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineCommunicator;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.utils.resources.ResourceInitializer;

public class InitHandler extends ResourceHandler {

    /**
     * Used by AWS Lambda
     */
    public InitHandler() {
        super(new Environment(), new CodePipelineCommunicator());
    }

    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
        throws IOException, URISyntaxException {

        String certificateArn = environment.readEnv(CERTIFICATE_ARN);

        SwaggerHubInfo swaggerHubInfo = initializeSwaggerHubInfo();
        StaticUrlInfo staticUrlInfo = initializeStaticUrlInfo();
        BranchInfo branchInfo = initalizeBranchInfo();
        ResourceInitializer initializer =
            new ResourceInitializer(stackName,
                staticUrlInfo,
                certificateArn,
                swaggerHubInfo,
                swaggerHubSecretsReader,
                stage,
                branchInfo,
                cloudFormationClient,
                apiGatewayClient
            );

        initializer.initializeStacks();

        return new SimpleResponse("OK");
    }
}
