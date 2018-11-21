package no.bibsys.lambda.api.handlers;


import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubConf;
import no.bibsys.lambda.api.requests.PullRequest;
import no.bibsys.lambda.api.requests.PushEvent;
import no.bibsys.lambda.api.requests.RepositoryInfo;
import no.bibsys.lambda.api.utils.SignatureChecker;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import no.bibsys.lambda.deploy.handlers.templates.ApiGatewayHandlerTemplate;
import no.bibsys.utils.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubHandler extends ApiGatewayHandlerTemplate<String, String> {

    private final transient static Logger logger = LoggerFactory.getLogger(GithubHandler.class);
    private final transient SwaggerHubInfo swaggerHubInfo;


    private transient SignatureChecker signatureChecker;



    public GithubHandler() {
        this(new Environment());

    }

    public GithubHandler(Environment environment) {
        super(String.class);

        String secretName = environment.readEnv(SignatureChecker.AWS_SECRET_NAME);
        String secretKey = environment.readEnv(SignatureChecker.AWS_SECRET_KEY);

        signatureChecker = new SignatureChecker(secretName, secretKey);
        swaggerHubInfo = new SwaggerHubInfo(environment);


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


    private String processPushEvent(PushEvent pushEvent) {
        return pushEvent.toString();

    }


    private String processPullRequest(PullRequest pullRequest)
        throws IOException, URISyntaxException {
        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)
            || pullRequest.getAction().equals(PullRequest.ACTION_REOPEN)) {
            createStacks(pullRequest, swaggerHubInfo);
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(pullRequest, swaggerHubInfo);
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


    protected void deleteStacks(RepositoryInfo repositoryInfo, SwaggerHubInfo swaggerHubInfo)
        throws IOException, URISyntaxException {
        GitInfo gitInfo = new GithubConf(repositoryInfo.getOwner(), repositoryInfo.getRepository());

        Application application = new Application(gitInfo, repositoryInfo.getBranch());
        application.wipeStacks(swaggerHubInfo);
    }

    protected void createStacks(RepositoryInfo repositoryInfo, SwaggerHubInfo swaggerHubInfo)
        throws IOException, URISyntaxException {
        GitInfo gitInfo = new GithubConf(repositoryInfo.getOwner(), repositoryInfo.getRepository());
        Application application = new Application(gitInfo, repositoryInfo.getBranch());
        application.createStacks(swaggerHubInfo);
    }


    public void setSignatureChecker(SignatureChecker signatureChecker) {
        this.signatureChecker = signatureChecker;
    }


}


