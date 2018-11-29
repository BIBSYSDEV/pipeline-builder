package no.bibsys.aws.lambda.api.handlers;


import java.io.IOException;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.api.requests.GitEvent;
import no.bibsys.aws.lambda.handlers.templates.ApiGatewayHandlerTemplate;

public abstract class ApiHandler extends ApiGatewayHandlerTemplate<String, String> {



    protected ApiHandler() {
        super(String.class);

    }



    protected void deleteStacks(GitEvent event) {
        GithubConf gitInfo =
            new GithubConf(event.getGitOwner(), event.getGitRepository(), event.getGitBranch());

        Application application = new Application(gitInfo);
        application.wipeStacks();
    }

    protected void createStacks(GitEvent event) throws IOException {
        GithubConf gitInfo =
            new GithubConf(event.getGitOwner(), event.getGitRepository(), event.getGitBranch());
        Application application = new Application(gitInfo);
        application.createStacks();
    }
}
