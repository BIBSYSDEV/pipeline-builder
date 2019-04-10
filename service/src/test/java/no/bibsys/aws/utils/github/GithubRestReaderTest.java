package no.bibsys.aws.utils.github;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.secrets.SecretsReader;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;

class GithubRestReaderTest {

    public static final String SUCCESS_REASON_PHRASE = "OK";
    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String BRANCH = "branch";
    private static final String URL = "http://www.example.org";
    private static final String EXPECTED_RESPONSE = "this is the expected response";
    private static final String PROTOCOL = "http";
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;
    private static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion(PROTOCOL, MAJOR_VERSION, MINOR_VERSION);
    private static final String BAD_CREDENTIALS_BODY = "{\"message\":\"Bad credentials\","
        + "\"documentation_url\":\"https://developer.github.com/v3\"}";

    private final transient static String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";
    private SecretsReader mockSecretsReader = () -> "something";
    private GithubConf githubConf = new GithubConf(OWNER, REPO, BRANCH, mockSecretsReader);

    private String createUrl(Path path, GithubConf githubConf) {
        String pathString = path.toString();
        return String.format(urlTemplate,
            githubConf.getOwner(),
            githubConf.getRepository(),
            githubConf.getBranch(),
            pathString
        );
    }

    @Test
    void ReadRestShouldReturnNonEmptyStringForValidUrlAndValidCredentials() throws IOException, UnauthorizedException {

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);
        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
        basicHttpEntity.setContent(new ByteArrayInputStream(EXPECTED_RESPONSE.getBytes()));
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getEntity()).thenReturn(basicHttpEntity);
        when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(PROTOCOL_VERSION, HttpStatus.SC_OK,
            SUCCESS_REASON_PHRASE));

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        String result = githubRestReader.readRest(URL);

        assertThat(result, is(equalTo(EXPECTED_RESPONSE)));
    }

    @Test
    void ReadRestShouldThrowExceptionOnInvalidCredentials() throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        when(mockHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion(PROTOCOL,
            MAJOR_VERSION, MINOR_VERSION), HttpStatus.SC_UNAUTHORIZED, BAD_CREDENTIALS_BODY));
        when(httpClient.execute(any())).thenReturn(mockHttpResponse);

        GithubRestReader githubRestReader = new GithubRestReader(httpClient, githubConf);
        String url = createUrl(Paths.get("service", "src", "main", "resources", "templates", "pipelineTemplate.yaml"),
            githubConf);
        assertThrows(UnauthorizedException.class, () -> githubRestReader.readRest(url));
    }

    @Test
    void ReadRestShouldThrowExceptionOnInvalidUrl() {

    }

    @Test
    void getGitInfo() {
    }
}
