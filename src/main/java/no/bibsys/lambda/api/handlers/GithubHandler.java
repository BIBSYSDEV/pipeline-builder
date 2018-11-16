package no.bibsys.lambda.api.handlers;


import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubConf;
import no.bibsys.lambda.api.utils.SignatureChecker;
import no.bibsys.lambda.api.requests.PullRequest;
import no.bibsys.lambda.api.requests.PushEvent;
import no.bibsys.lambda.api.requests.RepositoryInfo;
import no.bibsys.lambda.templates.ApiGatewayHandlerTemplate;
import no.bibsys.utils.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubHandler extends ApiGatewayHandlerTemplate<String, String> {

    private transient SignatureChecker signatureChecker;
    private final transient static Logger logger = LoggerFactory.getLogger(GithubHandler.class);
    private transient Environment environment;

    public GithubHandler() {
        super(String.class);
    }

    @Override
    public String processInput(String request, Map<String, String> headers, Context context)
        throws IOException {
        init();
        String webhookSecurityToken = headers.get("X-Hub-Signature");
        boolean verified = signatureChecker.verifySecurityToken(webhookSecurityToken, request);
        if (verified) {
            return processGitEvent(request);
        } else {
            throw new UnauthorizedException("Wrong API key signature");
        }

    }

    private String processGitEvent(String request) throws IOException {
        Optional<RepositoryInfo> gitEventOpt = parseEvent(request);
        String response = "No action";
        if (gitEventOpt.isPresent()) {
            RepositoryInfo repositoryInfo = gitEventOpt.get();
            if (repositoryInfo instanceof PullRequest) {
                response = processPullRequest((PullRequest) repositoryInfo);
            } else if (repositoryInfo instanceof PushEvent) {
                response = processPushEvent((PushEvent) repositoryInfo);
            }
        }
        return response;
    }


    private String processPushEvent(PushEvent pushEvent) throws IOException {
        return pushEvent.toString();

    }


    private String processPullRequest(PullRequest pullRequest) throws IOException {
        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)
            || pullRequest.getAction().equals(PullRequest.ACTION_REOPEN)) {
            createStacks(pullRequest);
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(pullRequest);
        }

        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return pullRequest.toString();


    }


    private Optional<RepositoryInfo> parseEvent(String json) throws IOException {
        Optional<RepositoryInfo> event = PullRequest.create(json);
        if (!event.isPresent()) {
            event = PushEvent.create(json);
        }
        return event;
    }


    protected void deleteStacks(RepositoryInfo repositoryInfo)
        throws IOException {
        GitInfo gitInfo = new GithubConf(repositoryInfo.getOwner(), repositoryInfo.getRepository(),
            new Environment());
        Application application = new Application(gitInfo, repositoryInfo.getBranch());
        application.wipeStacks();
    }

    protected void createStacks(RepositoryInfo repositoryInfo)
        throws IOException {
        GitInfo gitInfo = new GithubConf(repositoryInfo.getOwner(), repositoryInfo.getRepository(),
            new Environment());
        Application application = new Application(gitInfo, repositoryInfo.getBranch());
        application.createStacks();
    }




    private void init() {
        if (environment == null) {
            environment = new Environment();
        }

        signatureChecker = new SignatureChecker(environment);

    }




    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


}


