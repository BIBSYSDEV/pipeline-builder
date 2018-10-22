package no.bibsys.cloudformation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import no.bibsys.Application;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import no.bibsys.utils.AmazonRestrictions;
import no.bibsys.utils.MockEnvironment;
import no.bibsys.utils.StackBuilder;
import no.bibsys.utils.StackWiper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public abstract class ConfigurationTests extends AmazonRestrictions {

    protected final String projectId;
    private final GithubReader githubReader;
    protected String shortBranch;
    String branchName = "AUTREG-49_Delete_tables_from_DynamoDB_after_testing";
    Application application;
    PipelineStackConfiguration conf;
    GithubConf githubConf;


    protected RestReader restReader = Mockito.mock(RestReader.class);



    @Before
    public void init() throws IOException {
        when(restReader.readRest(anyString())).thenReturn("{\"download_url\":\"some_url\"}");
        when(restReader.getGithubConf()).thenReturn(this.githubConf);
    }




    public ConfigurationTests() throws IOException {
        githubConf=new GithubConf("OWNER","REPO",new MockEnvironment());
        init();
        this.githubReader=new GithubReader(restReader,branchName);
        this.application=new Application(githubReader);
        this.conf=new StackBuilder(new StackWiper(),githubReader).pipelineStackConfiguration();
        this.shortBranch = conf.getNormalizedBranchName();
        this.projectId = conf.getProjectId();
    }



    @Test
    public void mockRestReaderTest() throws IOException {
        String s1= restReader.readRest("lalala");
        String s2= restReader.readRest("lalala");
        String s3= restReader.readRest("lalala");

    }


}
