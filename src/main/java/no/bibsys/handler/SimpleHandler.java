package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
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

        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)
            || pullRequest.getAction().equals(PullRequest.ACTION_REOPEN)) {
            createStacks(initGithubReader(pullRequest));
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(initGithubReader(pullRequest));
        }

        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return request;

    }


    private GithubReader initGithubReader(PullRequest pullRequest) throws IOException {
        GithubConf githubConf=new GithubConf(pullRequest.getOwner(),pullRequest.getRepositoryName(),new Environment());
        RestReader restReader=new RestReader(githubConf);
        return new GithubReader(restReader,pullRequest.getBranch());
    }


    protected void deleteStacks(GithubReader reader)
        throws IOException {
        Application application = new Application(reader);
        application.wipeStacks();
    }

    protected void createStacks(GithubReader reader)
        throws IOException{

        Application application = new Application(reader);
        application.createStacks();

    }



}


