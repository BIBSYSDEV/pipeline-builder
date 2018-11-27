package no.bibsys.aws.lambda.api.handlers;


import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.git.github.GitInfoImpl;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.secrets.SignatureChecker;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.lambda.api.requests.PullRequest;
import no.bibsys.aws.lambda.api.requests.PushEvent;
import no.bibsys.aws.lambda.deploy.handlers.templates.ApiGatewayHandlerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubHandler extends ApiGatewayHandlerTemplate<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(GithubHandler.class);


    private transient SignatureChecker signatureChecker;



    public GithubHandler() {
        this(new Environment());

    }

    public GithubHandler(Environment environment) {
        super(String.class);

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
        Optional<GitInfo> gitEventOpt = parseEvent(request);
        String response = "No action";
        if (gitEventOpt.isPresent()) {
            GitInfo repositoryInfo = gitEventOpt.get();
            if (repositoryInfo instanceof PullRequest) {
                response = processPullRequest((PullRequest) repositoryInfo);
            } else if (repositoryInfo instanceof PushEvent) {
                response = processPushEvent((PushEvent) repositoryInfo);
            }
        }
        return response;
    }


    private String processPushEvent(PushEvent pushEvent) {
        return pushEvent.toString();

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


    private Optional<GitInfo> parseEvent(String json) throws IOException {
        Optional<GitInfo> event = PullRequest.create(json);
        if (!event.isPresent()) {
            event = PushEvent.create(json);
        }
        return event;
    }


    protected void deleteStacks(GitInfoImpl repositoryInfo) {
        GitInfo gitInfo =
                new GithubConf(repositoryInfo.getOwner(), repositoryInfo.getRepository(), repositoryInfo.getBranch());

        Application application = new Application(gitInfo);
        application.wipeStacks();
    }

    protected void createStacks(GitInfoImpl repositoryInfo) throws IOException {
        GitInfo gitInfo =
                new GithubConf(repositoryInfo.getOwner(), repositoryInfo.getRepository(), repositoryInfo.getBranch());
        Application application = new Application(gitInfo);
        application.createStacks();
    }


    public void setSignatureChecker(SignatureChecker signatureChecker) {
        this.signatureChecker = signatureChecker;
    }


}


