package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.handler.requests.PullRequest;
import no.bibsys.utils.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleHandler extends HandlerHelper<String, String> {


    private static final Logger logger = LogManager.getLogger(SimpleHandler.class);


    public SimpleHandler() {
        super(String.class);
    }

    @Override
    protected String processInput(String request, Context context) throws IOException {
        PullRequest pullRequest = new PullRequest(request);
        Environment env = new Environment();

        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)
            || pullRequest.getAction().equals(PullRequest.ACTION_REOPEN)) {
            createStacks(pullRequest, env);
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(pullRequest, env);
        }

        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return request;

    }


    private void deleteStacks(PullRequest pullRequest, Environment env)
        throws IOException {
        Application application = new Application(env);
        application
            .withRepoOwner(pullRequest.getOwner())

            .withRepoName(pullRequest.getRepositoryName())
            .withBranch(pullRequest.getBranch())
            .wipeStacks();
    }

    private void createStacks(PullRequest pullRequest, Environment env)
        throws IOException {
        Application application = new Application(env);
        application
            .withRepoOwner(pullRequest.getOwner())
            .withRepoName(pullRequest.getRepositoryName())
            .withBranch(pullRequest.getBranch())
            .createStacks();
    }


}


