package no.bibsys;

import java.io.IOException;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.utils.IoUtils;
import no.bibsys.utils.StackBuilder;
import no.bibsys.utils.StackWiper;

public class Application {

    private final transient IoUtils ioUtils = new IoUtils();
    private final transient StackWiper wiper;


    private final transient String repoName;
    private final transient String branch;
    private final transient String repoOwner;
    private final transient GithubReader githubReader;
    private final transient GithubConf githubConf;


    public Application(GithubReader githubReader) {

        this.githubConf = githubReader.getGithubConf();
        this.githubReader = githubReader;
        this.repoOwner = githubConf.getOwner();
        this.repoName = githubConf.getOwner();
        this.branch = githubReader.getBranch();
        wiper = new StackWiper();
        checkNulls();

    }


    public void createStacks() throws IOException {
        StackBuilder stackBuilder = new StackBuilder(wiper, githubReader);
        stackBuilder.createStacks();
    }


    public void wipeStacks() throws IOException {
        checkNulls();
        PipelineStackConfiguration conf = new PipelineStackConfiguration(githubReader);
        wiper.wipeStacks(conf);

    }


    private void checkNulls() {

        if (repoName == null) {
            throw new IllegalArgumentException("repoName is null");
        }
        if (branch == null) {
            throw new IllegalArgumentException("branch is null");
        }
        if (repoOwner == null) {
            throw new IllegalArgumentException("repoOwner is null");
        }

    }


}
