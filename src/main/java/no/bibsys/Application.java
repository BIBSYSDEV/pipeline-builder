package no.bibsys;

import com.google.common.base.Preconditions;
import java.io.IOException;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubReader;
import no.bibsys.roles.RoleManager;
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
        this.pipelineStackConfiguration=new PipelineStackConfiguration(githubReader);
        this.repoOwner = githubConf.getOwner();
        this.repoName = githubConf.getOwner();
        this.branch = githubReader.getBranch();
        wiper = new StackWiper(pipelineStackConfiguration);
        checkNulls();

    }


    public PipelineStackConfiguration getPipelineStackConfiguration() {
        return pipelineStackConfiguration;
    }

    public void createStacks() throws IOException {
        StackBuilder stackBuilder = new StackBuilder(wiper, pipelineStackConfiguration);
        stackBuilder.createStacks();
    }


    public void updateLambdaTrustRole(){

        RoleManager roleManager=new RoleManager(pipelineStackConfiguration.getPipelineConfiguration());
        roleManager.updateRole();

    }


    public void wipeStacks()  {
        checkNulls();
        wiper.wipeStacks();

    }


    private void checkNulls() {
        Preconditions.checkNotNull(repoName);
        Preconditions.checkNotNull(branch);
        Preconditions.checkNotNull(repoOwner);


    }


}
