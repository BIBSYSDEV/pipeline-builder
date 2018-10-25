package no.bibsys.github;

import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import no.bibsys.utils.MockEnvironment;
import org.junit.Ignore;
import org.junit.Test;

public class GithubReaderTest {


    private final static String owner = "BIBSYSDEV";
    private final static String repository = "authority-registry-infrastructure";


    private final transient GithubConf githubConf;
    private transient GithubReader githubReader;


    public GithubReaderTest() throws IOException {
        this.githubConf = new GithubConf(owner, repository, new MockEnvironment());
        this.githubReader = new GithubReader(new RestReader(githubConf),
            "autreg-54-do-not-delete-prod-stack");
    }


    @Test
    @Ignore()
    public void testConnection() throws IOException {
        String result = githubReader
            .readFile(Paths.get("lambdaTrustRoleConfig", "lambdaTrustRolePolicy.json"));
        System.out.print(result);


    }

}
