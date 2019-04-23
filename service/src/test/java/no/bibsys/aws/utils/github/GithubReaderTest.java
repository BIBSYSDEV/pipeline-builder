package no.bibsys.aws.utils.github;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.secrets.SecretsReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

public class GithubReaderTest extends GithubTestUtilities {

    private static final Path ARBITRARY_PATH = Paths.get("folder", "folder", "file");
    private static final String OWNER = "ownername";
    private static final String REPO = "reponame";
    private static final String BRANCH = "specialbranch";
    private static final transient String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";
    private static final SecretsReader SECRETS_READER = () -> "secret";
    private static final GithubConf githubConf = new GithubConf(OWNER, REPO, BRANCH,
        SECRETS_READER);

    @Test
    public void createUrkFromPathShouldThrowExceptionForNullGithubConf() {
        Path inputPath = Paths.get("directory/directory/file");

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        GithubReader githubReader = new GithubReader(httpClient);
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> githubReader.createUrl(inputPath));
        assertThat(exception.getMessage(), is(equalTo(GithubReader.GITHUBCONF_NULL_ERROR_MESSAGE)));

    }


    @Test
    public void createUrlFromPathShouldMapPathToValidUrl() {
        Path inputPath = Paths.get("directory/directory/file");
        String expectedUrl = String.format(urlTemplate, OWNER, REPO, BRANCH, inputPath);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        GithubReader githubReader = new GithubReader(httpClient);
        githubReader.setGitHubConf(githubConf);
        String outputUrl = githubReader.createUrl(inputPath);
        assertThat(outputUrl, is(equalTo(expectedUrl)));
    }

    @Test
    public void readFileShouldReturnAFileForAValidPath() throws IOException, UnauthorizedException, NotFoundException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);

        when(httpClient.execute(any())).thenReturn(response);
        when(response.getEntity()).thenReturn(simpleResponse);
        when(response.getStatusLine()).thenReturn(STATUS_LINE_OK);
        GithubReader githubReader = new GithubReader(httpClient);
        githubReader.setGitHubConf(githubConf);
        String responseString = githubReader.readFile(ARBITRARY_PATH);
        assertThat(responseString, is(equalTo(EXPECTED_RESPONSE)));
    }
}
