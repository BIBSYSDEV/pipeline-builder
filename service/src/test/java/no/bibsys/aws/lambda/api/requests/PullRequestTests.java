package no.bibsys.aws.lambda.api.requests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;

public class PullRequestTests {

    private static final String GITHUB_RESOURCE_FOLDER = "github";
    private static final String SAMPLE_PR_EVENT = "github_close_pull_request_payload.json";
    private static final String REPOSITORY_NAME = "Hello-World";
    private static final String BRANCH_NAME = "changes";
    private String pullRequestString;
    private PullRequest pullRequest;

    public PullRequestTests() throws IOException {
        pullRequestString = IoUtils.resourceAsString(Paths.get(GITHUB_RESOURCE_FOLDER,
            SAMPLE_PR_EVENT));
        pullRequest = (PullRequest) PullRequest.create(pullRequestString).get();
    }

    @Test
    public void create_githubPullRequestJson_validAction() {
        assertThat(pullRequest.getAction(), is(equalTo(PullRequest.ACTION_CLOSE)));
    }

    @Test
    public void create_githubPullRequestJson_branchName() {
        assertThat(pullRequest.getBranch(), is(equalTo(BRANCH_NAME)));
    }

    @Test
    public void create_githubPullRequestJson_repositoryName() {
        assertThat(pullRequest.getRepository(), is(equalTo(REPOSITORY_NAME)));
    }


}
