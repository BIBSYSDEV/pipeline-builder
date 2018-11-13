package no.bibsys;

import com.google.common.base.Preconditions;
import java.io.IOException;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubConf;
import no.bibsys.handler.requests.Action;
import no.bibsys.utils.Environment;
import no.bibsys.utils.StackBuilder;
import no.bibsys.utils.StackWiper;

public class Application {

    private final transient StackWiper wiper;


    private final transient String repoName;
    private final transient String branch;
    private final transient String repoOwner;
    private final transient PipelineStackConfiguration pipelineStackConfiguration;


    public Application(GitInfo gitInfo, String branch) {

        GitInfo githubConf = gitInfo;
        this.pipelineStackConfiguration = new PipelineStackConfiguration(githubConf,branch);
        this.repoOwner = githubConf.getOwner();
        this.repoName = githubConf.getOwner();
        this.branch =branch;
        wiper = new StackWiper(pipelineStackConfiguration);
        checkNulls();

    }

    public static void run(String repoOwner, String repository, String branch, String action)
        throws IOException {
        GitInfo gitInfo = new GithubConf(repoOwner, repository, new Environment());

        Application application = new Application(gitInfo,branch);
        if (action.equals(Action.CREATE)) {
            application.createStacks();
        } else if (action.equals(Action.DELETE)) {
            application.wipeStacks();
        }


    }

    public static void main(String args[]) throws IOException {
        String repoOwner = System.getProperty("owner");
        Preconditions.checkNotNull(repoOwner, "System property \"owner\" is not set");
        String repository = System.getProperty("repository");
        Preconditions.checkNotNull(repository, "System property \"repository\" is not set");
        String branch = System.getProperty("branch");
        Preconditions.checkNotNull(branch, "System property \"branch\" is not set");
        String action = System.getProperty("action");
        StringBuilder message = new StringBuilder(100);
        message.append("System property \"action\" is not set\n")
            .append("Valid values: create,delete,update-role");
        Preconditions.checkNotNull(action, message.toString());

        Application.run(repoOwner, repository, branch, action);

    }

    public PipelineStackConfiguration getPipelineStackConfiguration() {
        return pipelineStackConfiguration;
    }

    public void createStacks() throws IOException {
        StackBuilder stackBuilder = new StackBuilder(wiper, pipelineStackConfiguration);
        stackBuilder.createStacks();
    }



    public void wipeStacks() {
        checkNulls();
        wiper.wipeStacks();

    }

    private void checkNulls() {
        Preconditions.checkNotNull(repoName);
        Preconditions.checkNotNull(branch);
        Preconditions.checkNotNull(repoOwner);
    }


}
