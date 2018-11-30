package no.bibsys.aws.lambda.deploy.handlers;


import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.aws.lambda.responses.SimpleResponse;
import no.bibsys.aws.tools.Environment;

public abstract class ResourceHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {


    protected final transient String swagerApiId;
    protected final transient String swagerApiVersion;
    protected final transient String swagerApiOwner;
    protected  final transient Stage stage;
    protected final transient String stackName;
    protected final transient String zoneName;

    protected final transient String applicationUrl;


    protected final  transient Environment environment;


    public ResourceHandler(Environment environment) {
        super();
        this.environment = environment;

        this.applicationUrl = environment.readEnv(EnvironmentConstants.APPLICATION_URL);

        this.swagerApiId = environment.readEnv(EnvironmentConstants.SWAGGER_API_ID);
        this.swagerApiVersion = environment.readEnv(EnvironmentConstants.SWAGGER_API_VERSION);
        this.swagerApiOwner = environment.readEnv(EnvironmentConstants.SWAGGER_API_OWNER);

        this.zoneName = environment.readEnv(EnvironmentConstants.ZONE_NAME_ENV);
        this.stackName=environment.readEnv(EnvironmentConstants.STACK_NAME);

        this.stage = Stage.fromString(environment.readEnv(EnvironmentConstants.STAGE));

    }
}
