package no.bibsys.aws.lambda.deploy.handlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.BranchInfo;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.events.DeployEvent;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineCommunicator;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.secrets.AwsSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.tools.Environment;

import java.io.IOException;
import java.net.URISyntaxException;

public abstract class ResourceHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {

    private static final String OVERRIDE_WARNING = "You should override this method";
    protected transient Stage stage;
    protected transient String stackName;
    protected transient String branch;
    protected transient Environment environment;
    protected transient AmazonCloudFormation cloudFormationClient;
    protected transient AmazonApiGateway apiGatewayClient;
    protected transient SecretsReader swaggerHubSecretsReader;
    protected transient AmazonRoute53 route53Client;
    private transient String swaggerApiId;
    private transient String swaggerApiVersion;
    private transient String swaggerApiOwner;
    private transient String zoneName;
    private transient String applicationUrl;

    public ResourceHandler(Environment environment,
        CodePipelineCommunicator codePipelineCommunicator) {
        super(codePipelineCommunicator);
        this.environment = environment;


    }

    // Read all ENV in processInput so that in case of failure the error will be handled
    // by the  no.bibsys.aws.lambda.handlers.templates.HandlerTemplate class and the Lambda function will terminate
    // soon and not wait for the 30 minutes timeout.
    protected void init() {
        this.branch = environment.readEnv(EnvironmentConstants.BRANCH);
        this.applicationUrl = environment.readEnv(EnvironmentConstants.APPLICATION_URL);

        this.swaggerApiId = environment.readEnv(EnvironmentConstants.SWAGGER_API_ID);
        this.swaggerApiVersion = environment.readEnv(EnvironmentConstants.SWAGGER_API_VERSION);
        this.swaggerApiOwner = environment.readEnv(EnvironmentConstants.SWAGGER_API_OWNER);

        this.zoneName = environment.readEnv(EnvironmentConstants.ZONE_NAME_ENV);
        this.stackName = environment.readEnv(EnvironmentConstants.STACK_NAME);

        this.stage = Stage.fromString(environment.readEnv(EnvironmentConstants.STAGE));

        this.cloudFormationClient = AmazonCloudFormationClientBuilder.defaultClient();
        this.apiGatewayClient = AmazonApiGatewayClientBuilder.defaultClient();
        this.route53Client = AmazonRoute53ClientBuilder.defaultClient();

        String swaggerHubApiKeySecretsName = environment
            .readEnv(EnvironmentConstants.ACCESS_SWAGGERHUB_SECRET_NAME);
        String swaggerHubApiKeySecretsKey = environment
            .readEnv(EnvironmentConstants.ACCESS_SWAGGERHUB_SECRET_KEY);
        Region region = Region
            .getRegion(Regions.fromName(environment.readEnv(EnvironmentConstants.AWS_REGION)));
        
        this.swaggerHubSecretsReader = new AwsSecretsReader(swaggerHubApiKeySecretsName,
            swaggerHubApiKeySecretsKey, region);
    }

    @Override
    protected SimpleResponse processInput(DeployEvent deployEvent, String apiGatewayQuery, Context context)
        throws IOException, URISyntaxException {
        init();
        throw new IllegalStateException(OVERRIDE_WARNING);
    }

    protected SwaggerHubInfo initializeSwaggerHubInfo() {

        return new SwaggerHubInfo(swaggerApiId, swaggerApiVersion, swaggerApiOwner,
            swaggerHubSecretsReader);
    }

    protected StaticUrlInfo initializeStaticUrlInfo() {
        return new StaticUrlInfo(zoneName, applicationUrl, stage);
    }

    protected BranchInfo initalizeBranchInfo() {
        BranchInfo branchInfo = new BranchInfo();
        branchInfo.setBranch(branch);
        return branchInfo;
    }
}
