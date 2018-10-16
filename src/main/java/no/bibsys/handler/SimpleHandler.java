package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.utils.Environment;
import no.bibsys.handler.requests.PullRequest;
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
        String projectName=env.readEnvOpt("PROJECT_NAME").orElseThrow(()->
            missingEnvVariable("PROJECT_NAME") );

        if(pullRequest.getAction().equals(PullRequest.ACTION_OPEN)){
            createStacks(pullRequest, env, projectName);
        }

        if(pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)){
            deleteStacks(pullRequest, env,projectName);
        }



        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return request;

    }

    private void deleteStacks(PullRequest pullRequest, Environment env,String projectName) throws IOException {
        Application application=new Application(env);
        application
            .withRepoOwner(pullRequest.getOwner())
            .withProjectName(projectName)
            .withRepoName(pullRequest.getRepositoryName())
            .withBranch(pullRequest.getBranch())
            .wipeStacks();
    }

    private void createStacks(PullRequest pullRequest, Environment env, String projectName)
        throws IOException {
        Application application=new Application(env);
        application
            .withRepoOwner(pullRequest.getOwner())
            .withProjectName(projectName)
            .withRepoName(pullRequest.getRepositoryName())
            .withBranch(pullRequest.getBranch())
            .createStacks();
    }


    private IllegalArgumentException missingEnvVariable(String variableName){
        String message=String.format("Missing %s environment variable.%n"
            + "Add the variable in your cloud-formation template.",variableName);
        return new IllegalArgumentException(message);
    }


}


