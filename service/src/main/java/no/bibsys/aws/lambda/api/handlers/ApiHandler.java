package no.bibsys.aws.lambda.api.handlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.s3.AmazonS3;
import java.io.IOException;
import java.util.Map;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.GitEvent;
import no.bibsys.aws.lambda.handlers.templates.ApiGatewayHandlerTemplate;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

public abstract class ApiHandler extends ApiGatewayHandlerTemplate<String, String> {

    private static final String OVERRIDE_WARNING = "You should override this method";

    protected final transient Environment environment;
    private final transient AmazonCloudFormation cloudFormation;
    private final transient AmazonS3 s3Client;
    private final transient AWSLambda lambdaClient;
    private final transient AWSLogs logsClient;
    protected transient Region region;

    protected ApiHandler(Environment environment,
        AmazonCloudFormation acf,
        AmazonS3 s3Client,
        AWSLambda lambdaClient,
        AWSLogs logsClient

    ) {
        super(String.class);
        this.environment = environment;
        this.cloudFormation = acf;
        this.s3Client = s3Client;
        this.lambdaClient = lambdaClient;
        this.logsClient = logsClient;
        this.region = Region
            .getRegion(Regions.fromName(environment.readEnv(EnvironmentConstants.AWS_REGION)));
    }

    // Read all ENV in processInput so that in case of failure the error will be handled
    // by the  no.bibsys.aws.lambda.handlers.templates.HandlerTemplate class and the Lambda function will terminate
    // soon and not wait for the 30 minutes timeout.
    protected void init() {
        this.region = Region
            .getRegion(Regions.fromName(environment.readEnv(EnvironmentConstants.AWS_REGION)));
    }

    @Override
    protected String processInput(String input, Map<String, String> headers, Context context) throws IOException {
        init();
        throw new IllegalStateException(OVERRIDE_WARNING);
    }

    protected void deleteStacks(GitEvent event) {
        GithubConf gitInfo =
            new GithubConf(event.getOwner(), event.getRepository(), event.getBranch(),
                readFromGithubSecretReader());

        Application application = new Application(gitInfo, cloudFormation, s3Client, lambdaClient,
            logsClient);
        application.wipeStacks();
    }

    protected void createStacks(GitEvent event) throws IOException {
        GithubConf gitInfo =
            new GithubConf(event.getOwner(), event.getRepository(), event.getBranch(),
                readFromGithubSecretReader());
        Application application = new Application(gitInfo, cloudFormation, s3Client, lambdaClient,
            logsClient);
        application.createStacks(cloudFormation);
    }

    protected void setRegionOrReportErrorToLogger() {
        init();
    }

    protected abstract SecretsReader readFromGithubSecretReader();
}
