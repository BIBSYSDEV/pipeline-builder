package no.bibsys.aws.utils.github;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import no.bibsys.aws.git.github.GithubConf;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

class GithubRestReaderTest extends GithubTestUtilities {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String BRANCH = "branch";
    private static final String URL = "http://www.example.org";
    private static final String ACCEPT = "Accept";
    private static final String EXPECTED_RAW_HEADER = "application/vnd.github.VERSION.raw";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TOKEN = "token";

    private GithubConf githubConf = new GithubConf(OWNER, REPO, BRANCH, MOCK_SECRETS_READER);

    @Test
    public void executeRequestShouldReturnNonEmptyStringForValidUrlAndValidCredentials()
            throws IOException, UnauthorizedException, NotFoundException {

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        when(httpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getEntity()).thenReturn(simpleResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_OK);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        String result = githubRestReader.executeRequest(githubRestReader.createRequest(URL));

        assertThat(result, is(equalTo(EXPECTED_RESPONSE)));
    }

    @Test
    public void executeRequestShouldThrowExceptionOnInvalidCredentials() throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_UNAUTHORIZED);
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        assertThrows(UnauthorizedException.class,
            () -> githubRestReader.executeRequest(githubRestReader.createRequest(URL)));
    }

    @Test
    public void executeRequestShouldThrowExceptionOnInvalidUrl() throws IOException {

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_NOT_FOUND);
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        assertThrows(NotFoundException.class,
            () -> githubRestReader.executeRequest(githubRestReader.createRequest(URL)));
    }

    @Test
    public void executeRequestShouldThrowNullPointerExceptionOnNullResponseContent() throws IOException {

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(STATUS_LINE_OK);

        when(mockHttpResponse.getEntity()).thenReturn(null);
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        assertThrows(NullPointerException.class,
            () -> githubRestReader.executeRequest(githubRestReader.createRequest(URL)));
    }

    @Test
    public void createRequestSetsHeaderForReadingRawContent() throws IOException {
        GithubRestReader githubRestReader = new GithubRestReader(null, githubConf);
        HttpGet httpGet = githubRestReader.createRequest(URL);
        Map<String, String> headerMap = Arrays.stream(httpGet.getAllHeaders())
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        assertThat(headerMap.keySet(), hasItem(ACCEPT));
        assertThat(headerMap.get(ACCEPT), is(equalTo(EXPECTED_RAW_HEADER)));
    }

    @Test
    public void createRequestSetsHeaderForAuthorization() throws IOException {
        GithubRestReader githubRestReader = new GithubRestReader(null, githubConf);
        HttpGet httpGet = githubRestReader.createRequest(URL);
        Map<String, String> headerMap = Arrays.stream(httpGet.getAllHeaders())
            .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        assertThat(headerMap.keySet(), hasItem(AUTHORIZATION));
        assertThat(headerMap.get(AUTHORIZATION), containsString(TOKEN));
    }
}
