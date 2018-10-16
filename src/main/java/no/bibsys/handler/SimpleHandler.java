package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.utils.Environment;
import no.bibsys.utils.PullRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleHandler extends HandlerHelper<String, String> {


    static final Logger logger = LogManager.getLogger(SimpleHandler.class);

    public SimpleHandler() {
        super(String.class, String.class);
    }

    protected String processInput(String request, Context context) throws IOException {
        PullRequest pullRequest = new PullRequest(request);
        Environment env=new Environment();
        Application application=new Application(env);
        if(pullRequest.getAction().equals(PullRequest.ACTION_OPEN)){
            application
                .withRepoOwner(pullRequest.getOwner())
                .withProjectName("lambapipe")
                .withRepoName(pullRequest.getRepositoryName())
                .withBranch(pullRequest.getBranch())
                .createStacks();

        }

        if(pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)){
            application
                .withRepoOwner(pullRequest.getOwner())
                .withProjectName("lambapipe")
                .withRepoName(pullRequest.getRepositoryName())
                .withBranch(pullRequest.getBranch())
                .wipeStacks();

        }



        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return request;

    }


}


