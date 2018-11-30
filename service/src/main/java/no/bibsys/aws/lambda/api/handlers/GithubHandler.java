package no.bibsys.aws.lambda.api.handlers;


import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import no.bibsys.aws.lambda.api.requests.GitEvent;
import no.bibsys.aws.lambda.api.requests.PullRequest;
import no.bibsys.aws.secrets.SignatureChecker;
import no.bibsys.aws.tools.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubHandler extends ApiHandler {

    private static final Logger logger = LoggerFactory.getLogger(GithubHandler.class);


    private transient SignatureChecker signatureChecker;



    public GithubHandler() {
        this(new Environment());

    }

    public GithubHandler(Environment environment) {
        super();
        String secretName = environment.readEnv(SignatureChecker.AWS_SECRET_NAME);
        String secretKey = environment.readEnv(SignatureChecker.AWS_SECRET_KEY);

        signatureChecker = new SignatureChecker(secretName, secretKey);



    }

    @Override
    public String processInput(String request, Map<String, String> headers, Context context)
            throws IOException, URISyntaxException {

        String webhookSecurityToken = headers.get("X-Hub-Signature");
        boolean verified = signatureChecker.verifySecurityToken(webhookSecurityToken, request);

        if (verified) {
            return processGitEvent(request);
        } else {
            throw new UnauthorizedException("Wrong API key signature");
        }

    }


    private String processGitEvent(String request) throws IOException, URISyntaxException {
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





    private String processPullRequest(PullRequest pullRequest) throws IOException, URISyntaxException {
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
        Optional<GitEvent> event = PullRequest.create(json);
        return event;
    }





    public void setSignatureChecker(SignatureChecker signatureChecker) {
        this.signatureChecker = signatureChecker;
    }


}


