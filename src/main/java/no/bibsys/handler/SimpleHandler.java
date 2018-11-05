package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import no.bibsys.handler.requests.RepositoryInfo;
import no.bibsys.handler.requests.PullRequest;
import no.bibsys.handler.requests.PushEvent;
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
        Optional<RepositoryInfo> gitEventOpt=parseEvent(request);
        String response="No action";
        if(gitEventOpt.isPresent()){
            RepositoryInfo repositoryInfo =gitEventOpt.get();
               if(repositoryInfo instanceof PullRequest){
                   response=processPullRequest((PullRequest) repositoryInfo);
               }
               else if(repositoryInfo instanceof PushEvent){
                   response=processPushEvent((PushEvent) repositoryInfo);
               }
        }

        return response;

    }




    private String processPushEvent(PushEvent pushEvent) throws IOException {
        GithubReader githubReader=initGithubReader(pushEvent);
        Application application=new Application(githubReader);
        application.updateLambdaTrustRole();
        return pushEvent.toString();

    }


    private String processPullRequest(PullRequest pullRequest) throws IOException {
        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)
            || pullRequest.getAction().equals(PullRequest.ACTION_REOPEN)) {
            createStacks(initGithubReader(pullRequest));
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(initGithubReader(pullRequest));
        }

        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return  pullRequest.toString();



    }


    private Optional<RepositoryInfo> parseEvent(String json) throws IOException {
        Optional<RepositoryInfo> event= PullRequest.create(json);
        if(!event.isPresent()) {
            event = PushEvent.create(json);
        }
        return event;
    }


    private GithubReader initGithubReader(RepositoryInfo repositoryInfo) throws IOException {
        GithubConf githubConf = new GithubConf(repositoryInfo.getOwner(),
            repositoryInfo.getRepository(), new Environment());
        RestReader restReader = new RestReader(githubConf);
        return new GithubReader(restReader, repositoryInfo.getBranch());
    }


    protected void deleteStacks(GithubReader reader)
        throws IOException {
        Application application = new Application(reader);
        application.wipeStacks();
    }

    protected void createStacks(GithubReader reader)
        throws IOException {

        Application application = new Application(reader);
        application.createStacks();
    }


}


