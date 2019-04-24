package no.bibsys.aws;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;

class ApplicationTest extends LocalStackTest {

    private final transient Application application;

    public ApplicationTest() {
        GithubConf githubConf = new GithubConf("owner", "repository", "branch",
            mockSecretsReader());
        application = new Application(githubConf,
            mockCloudFormationWithStack(),
            initializeS3(),
            initializeLambdaClient(),
            initializeMockLogsClient());
    }

    @Test
    public void getPipelineStackConfiguration_Application_notNull() {
        assertThat(application.getPipelineStackConfiguration(), is(not(equalTo(null))));
    }
}
