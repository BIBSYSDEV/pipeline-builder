package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)) {
            createStacks(pullRequest, env, createProjectName(pullRequest.getRepositoryName()));
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(pullRequest, env, createProjectName(pullRequest.getRepositoryName()));
        }

        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return request;

    }


    private String createProjectName(String repositoryName) {
        String[] words = repositoryName.replaceAll("_","-").split("-");
        List<String> wordList = Arrays.asList(words).stream().map(this::shorten)
            .collect(Collectors.toList());
        return String.join("-", wordList);
    }

    private String shorten(String word) {
        int maxIndex = Math.min(word.length(), 3);
        return word.substring(0, maxIndex);
    }

    private void deleteStacks(PullRequest pullRequest, Environment env, String projectName)
        throws IOException {
        Application application = new Application(env);
        application
            .withRepoOwner(pullRequest.getOwner())
            .withProjectName(projectName)
            .withRepoName(pullRequest.getRepositoryName())
            .withBranch(pullRequest.getBranch())
            .wipeStacks();
    }

    private void createStacks(PullRequest pullRequest, Environment env, String projectName)
        throws IOException {
        Application application = new Application(env);
        application
            .withRepoOwner(pullRequest.getOwner())
            .withProjectName(projectName)
            .withRepoName(pullRequest.getRepositoryName())
            .withBranch(pullRequest.getBranch())
            .createStacks();
    }


}


