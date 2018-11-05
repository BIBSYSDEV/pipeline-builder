package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubConf;
import no.bibsys.handler.requests.PullRequest;
import no.bibsys.handler.requests.PushEvent;
import no.bibsys.handler.requests.RepositoryInfo;
import no.bibsys.utils.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleHandler extends HandlerHelper<String, String> {


    private static final Logger logger = LogManager.getLogger(SimpleHandler.class);


    public SimpleHandler() {
        super(String.class);
    }

    @Override
    public String processInput(String request, Context context) throws IOException {
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

        return  pullRequest.toString();



    }


    private Optional<RepositoryInfo> parseEvent(String json) throws IOException {
        Optional<RepositoryInfo> event= PullRequest.create(json);
        if(!event.isPresent()) {
            event = PushEvent.create(json);
        }
        return event;
    }



    protected void deleteStacks(RepositoryInfo repositoryInfo)
        throws IOException {
        GitInfo gitInfo=new GithubConf(repositoryInfo.getOwner(),repositoryInfo.getRepository(),new Environment());
        Application application = new Application(gitInfo,repositoryInfo.getBranch());
        application.wipeStacks();
    }

    protected void createStacks(RepositoryInfo repositoryInfo)
        throws IOException {
        GitInfo gitInfo=new GithubConf(repositoryInfo.getOwner(),repositoryInfo.getRepository(),new Environment());
        Application application = new Application(gitInfo,repositoryInfo.getBranch());
        application.createStacks();
    }


}


