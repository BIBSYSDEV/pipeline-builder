package no.bibsys.aws.lambda.api.handlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.s3.AmazonS3;
import java.io.IOException;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.GitEvent;
import no.bibsys.aws.lambda.handlers.templates.ApiGatewayHandlerTemplate;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

public abstract class ApiHandler extends ApiGatewayHandlerTemplate<String, String> {

    protected final transient Region region;

    private final transient AmazonCloudFormation cloudFormation;
    private final transient AmazonS3 s3Client;
    private final transient AWSLambda lambdaClient;
    private final transient AWSLogs logsClient;
    protected final transient Environment environment;

    protected final transient String readFromGithubSecretName;
    protected final transient String readFromGithubSecretKey;

    protected ApiHandler(Environment environment,
        AmazonCloudFormation acf,
        AmazonS3 s3Client,
        AWSLambda lambdaClient,
        AWSLogs logsClient

    ) {
        super(String.class);
        this.environment = environment;
        readFromGithubSecretName = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_NAME);
        readFromGithubSecretKey = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_KEY);

        this.region = Region
            .getRegion(Regions.fromName(environment.readEnv(EnvironmentConstants.AWS_REGION)));
        this.cloudFormation = acf;
        this.s3Client = s3Client;
        this.lambdaClient = lambdaClient;
        this.logsClient = logsClient;
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
        application.createStacks();
    }

    protected abstract SecretsReader readFromGithubSecretReader();
}
