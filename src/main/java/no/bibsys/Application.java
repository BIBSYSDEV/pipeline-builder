package no.bibsys;

import com.google.common.base.Preconditions;
import java.io.IOException;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubReader;
import no.bibsys.utils.StackBuilder;
import no.bibsys.utils.StackWiper;

public class Application {

    private final transient StackWiper wiper;


    private final transient String repoName;
    private final transient String branch;
    private final transient String repoOwner;

    private final transient PipelineStackConfiguration pipelineStackConfiguration;


    public Application(GithubReader githubReader) throws IOException {

        GitInfo githubConf = githubReader.getGitInfo();
        this.repoOwner = githubConf.getOwner();
        this.repoName = githubConf.getOwner();
        this.branch = githubReader.getBranch();
        this.pipelineStackConfiguration=new PipelineStackConfiguration(githubReader);
        wiper = new StackWiper();
        checkNulls();

    }


    public PipelineStackConfiguration getPipelineStackConfiguration() {
        return pipelineStackConfiguration;
    }

    public void createStacks() throws IOException {
        StackBuilder stackBuilder = new StackBuilder(pipelineStackConfiguration);
        stackBuilder.createStacks();
    }


    public void wipeStacks()  {
        checkNulls();
        wiper.wipeStacks(pipelineStackConfiguration);

    }


    private void checkNulls() {
        Preconditions.checkNotNull(repoName);
        Preconditions.checkNotNull(branch);
        Preconditions.checkNotNull(repoOwner);


    }


}
