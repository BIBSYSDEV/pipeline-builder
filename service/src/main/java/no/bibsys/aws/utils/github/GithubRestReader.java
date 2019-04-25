package no.bibsys.aws.utils.github;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.tools.IoUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

public class GithubRestReader {

    protected static final String GITHUBCONF_NULL_ERROR_MESSAGE = "You need to set the githubConf";
    private static final String BAD_GITHUB_CREDENTIALS = "Bad Github credentials";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN = "token ";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_FORMAT = "application/vnd.github.VERSION.raw";
    private static final String ERROR_MESSAGE = "Response HttpEntity was null";
    private final transient CloseableHttpClient httpClient;
    private transient GithubConf githubConf;

    public GithubRestReader(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Optional<String> executeRequest(HttpGet httpGet)
        throws IOException, UnauthorizedException {
        CloseableHttpResponse response = this.httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = null;
        switch (statusCode) {
            case HttpStatus.SC_UNAUTHORIZED:
                handleUnauthorized();
                break;
            case HttpStatus.SC_NOT_FOUND:
                return handleNotFound();
            case HttpStatus.SC_OK:
            default:
                responseString = handleSuccess(response);
                break;
        }

        Objects.requireNonNull(responseString, ERROR_MESSAGE);
        return Optional.of(responseString);
    }

    private Optional<String> handleNotFound() {
        return Optional.empty();
    }

    private void handleUnauthorized() throws UnauthorizedException {
        throw new UnauthorizedException(BAD_GITHUB_CREDENTIALS);
    }

    private String handleSuccess(CloseableHttpResponse response) throws IOException {
        HttpEntity responseEntity = response.getEntity();
        return IoUtils.streamToString(responseEntity.getContent());
    }

    public GithubRestReader setGitHubConf(GithubConf gitHubConf) {
        this.githubConf = gitHubConf;
        return this;
    }

    public HttpGet createRequest(String url) throws IOException {
        Objects.requireNonNull(githubConf, GITHUBCONF_NULL_ERROR_MESSAGE);
        HttpGet get = new HttpGet(url);
        get.setHeader(new BasicHeader(AUTHORIZATION, TOKEN + githubConf.getOauth()));
        get.setHeader(new BasicHeader(ACCEPT, ACCEPT_FORMAT));
        return get;
    }
}
