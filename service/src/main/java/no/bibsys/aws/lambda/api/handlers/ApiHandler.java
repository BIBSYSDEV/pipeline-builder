package no.bibsys.aws.lambda.api.handlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import java.io.IOException;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.GitEvent;
import no.bibsys.aws.lambda.handlers.templates.ApiGatewayHandlerTemplate;
import no.bibsys.aws.secrets.AWSSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

public abstract class ApiHandler extends ApiGatewayHandlerTemplate<String, String> {

    protected final transient Region region;
    private final transient SecretsReader secretsReader;

    protected ApiHandler(Environment environment) {
        super(String.class);
        String readFromGithubSecretName = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_NAME);
        String readFromGithubSecretKey = environment
            .readEnv(EnvironmentConstants.READ_FROM_GITHUB_SECRET_KEY);

        this.region = Region
            .getRegion(Regions.fromName(environment.readEnv(EnvironmentConstants.AWS_REGION)));
        this.secretsReader = new AWSSecretsReader(readFromGithubSecretName, readFromGithubSecretKey,
            region);
    }

    protected void deleteStacks(GitEvent event) {
        GithubConf gitInfo =
            new GithubConf(event.getOwner(), event.getRepository(), event.getBranch(),
                secretsReader);

        Application application = new Application(gitInfo);
        application.wipeStacks();
    }

    protected void createStacks(GitEvent event) throws IOException {
        GithubConf gitInfo =
            new GithubConf(event.getOwner(), event.getRepository(), event.getBranch(),
                secretsReader);
        Application application = new Application(gitInfo);
        application.createStacks();
    }
}
