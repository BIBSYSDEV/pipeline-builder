package no.bibsys.aws.utils.github;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import no.bibsys.aws.git.github.GithubConf;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

class GithubRestReaderTest extends GithubTestUtilities {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String BRANCH = "branch";
    private static final String URL = "http://www.example.org";

    private GithubConf githubConf = new GithubConf(OWNER, REPO, BRANCH, MOCK_SECRETS_READER);

    @Test
    public void ReadRestShouldReturnNonEmptyStringForValidUrlAndValidCredentials()
        throws IOException, UnauthorizedException, NotFoundException {

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        when(httpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getEntity()).thenReturn(SIMPLE_RESPONSE);
        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_OK);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        String result = githubRestReader.readRest(URL);

        assertThat(result, is(equalTo(EXPECTED_RESPONSE)));
    }

    @Test
    public void ReadRestShouldThrowExceptionOnInvalidCredentials() throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_UNAUTHORIZED);
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        assertThrows(UnauthorizedException.class, () -> githubRestReader.readRest(URL));
    }

    @Test
    public void ReadRestShouldThrowExceptionOnInvalidUrl() throws IOException {

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_NOT_FOUND);
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        assertThrows(NotFoundException.class, () -> githubRestReader.readRest(URL));
    }

    @Test
    public void ReadRestShouldThrowNullPointerExceptionOnNullResponseContent() throws IOException {

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_OK);

        when(mockHttpResponse.getEntity()).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        assertThrows(NullPointerException.class, () -> githubRestReader.readRest(URL));
    }
}
