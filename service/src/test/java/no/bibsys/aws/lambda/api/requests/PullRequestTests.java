package no.bibsys.aws.lambda.api.requests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;

public class PullRequestTests {


    private String pullRequestString;
    private PullRequest pullRequest;

    public PullRequestTests() throws IOException {
        pullRequestString = IoUtils.resourceAsString(Paths.get("github", "pullrequest.json"));
        pullRequest = (PullRequest) PullRequest.create(pullRequestString).get();
    }

    @Test
    public void create_githubPullRequestJson_validAction() {
        assertThat(pullRequest.getAction(), is(equalTo(PullRequest.ACTION_CLOSE)));
    }

    @Test
    public void create_githubPullRequestJson_branchName() {
        assertThat(pullRequest.getBranch(), is(equalTo("changes")));
    }

    @Test
    public void create_githubPullRequestJson_repositoryName() {
        assertThat(pullRequest.getRepository(), is(equalTo("Hello-World")));
    }


}
