package no.bibsys.aws.lambda.api.requests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;

public class SimplePullRequestTests {

    private static final String GITHUB_RESOURCE_FOLDER = "github";
    private static final String SAMPLE_PR_EVENT = "github_close_pull_request_payload.json";
    private static final String REPOSITORY_NAME = "Hello-World";
    private static final String BRANCH_NAME = "changes";
    private String pullRequestString;
    private SimplePullRequest simplePullRequest;

    public SimplePullRequestTests() throws IOException {
        pullRequestString = IoUtils.resourceAsString(Paths.get(GITHUB_RESOURCE_FOLDER,
            SAMPLE_PR_EVENT));
        simplePullRequest = (SimplePullRequest) SimplePullRequest.create(pullRequestString).get();
    }

    @Test
    public void create_githubPullRequestJson_validAction() {
        assertThat(simplePullRequest.getAction(), is(equalTo(SimplePullRequest.ACTION_CLOSE)));
    }

    @Test
    public void create_githubPullRequestJson_branchName() {
        assertThat(simplePullRequest.getBranch(), is(equalTo(BRANCH_NAME)));
    }

    @Test
    public void create_githubPullRequestJson_repositoryName() {
        assertThat(simplePullRequest.getRepository(), is(equalTo(REPOSITORY_NAME)));
    }


}
