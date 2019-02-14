package no.bibsys.aws.lambda.api.handlers;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.requests.GitEvent;
import no.bibsys.aws.lambda.api.requests.PullRequest;
import no.bibsys.aws.secrets.AWSSecretsReader;
import no.bibsys.aws.secrets.GithubSignatureChecker;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubHandler extends ApiHandler {

    private static final Logger logger = LoggerFactory.getLogger(GithubHandler.class);

    private transient GithubSignatureChecker signatureChecker;

    /**
     * Used by AWS Lambda.
     */
    public GithubHandler() {
        this(new Environment());
    }

    public GithubHandler(Environment environment) {
        super(environment);
        String secretName = environment.readEnv(EnvironmentConstants.GITHUB_WEBHOOK_SECRET_NAME);
        String secretKey = environment.readEnv(EnvironmentConstants.GITHUB_WEBHOOK_SECRET_KEY);
        String regsionString = environment.readEnv(EnvironmentConstants.AWS_REGION);
        Region region = Region.getRegion(Regions.fromName(regsionString));
        SecretsReader secretsReader = new AWSSecretsReader(secretName, secretKey, region);
        signatureChecker = new GithubSignatureChecker(secretsReader);
    }

    @Override
    public String processInput(String request, Map<String, String> headers, Context context)
        throws IOException {

        String webhookSecurityToken = headers.get("X-Hub-Signature");
        boolean verified = signatureChecker.verifySecurityToken(webhookSecurityToken, request);

        if (verified) {
            return processGitEvent(request);
        } else {
            throw new UnauthorizedException("Wrong API key signature");
        }
    }

    private String processGitEvent(String request) throws IOException {
        Optional<GitEvent> gitEventOpt = parseEvent(request);
        String response = "No action";
        if (gitEventOpt.isPresent()) {
            GitEvent event = gitEventOpt.get();
            if (event instanceof PullRequest) {
                response = processPullRequest((PullRequest) event);
            }
        }
        return response;
    }

    private String processPullRequest(PullRequest pullRequest)
        throws IOException {
        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)
            || pullRequest.getAction().equals(PullRequest.ACTION_REOPEN)) {
            createStacks(pullRequest);
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(pullRequest);
        }

        logger.info(pullRequest.toString());

        return pullRequest.toString();
    }

    private Optional<GitEvent> parseEvent(String json) throws IOException {
        return PullRequest.create(json);
    }

    public void setSignatureChecker(GithubSignatureChecker signatureChecker) {
        this.signatureChecker = signatureChecker;
    }
}


