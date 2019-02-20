package no.bibsys.aws.lambda.deploy.handlers;

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
import no.bibsys.aws.utils.resources.ResourceDestroyer;
import no.bibsys.aws.utils.resources.SwaggerHubConnectionDetails;

public class DestroyHandler extends ResourceHandler {

    /**
     * Used by AWS Lambda.
     */
    public DestroyHandler() {
        this(new Environment(), new CodePipelineCommunicator());
    }

    public DestroyHandler(Environment environment,
        CodePipelineCommunicator codePipelineCommunicator) {
        super(environment, codePipelineCommunicator);
    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {

        SwaggerHubInfo swaggerHubInfo = initializeSwaggerHubInfo();
        StaticUrlInfo staticUrlInfo = initializeStaticUrlInfo();
        BranchInfo branchInfo = initalizeBranchInfo();

        ResourceDestroyer resourceDestroyer = new ResourceDestroyer(
            stackName,
            staticUrlInfo,
            new SwaggerHubConnectionDetails(swaggerHubInfo, swaggerHubSecretsReader),
            stage,
            branchInfo,
            cloudFormationClient,
            apiGatewayClient,
            route53Client
        );
        resourceDestroyer.destroy();

        return new SimpleResponse("OK");
    }
}
