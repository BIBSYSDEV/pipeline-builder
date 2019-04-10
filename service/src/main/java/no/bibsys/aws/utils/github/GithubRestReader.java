package no.bibsys.aws.utils.github;

import java.io.IOException;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.tools.IoUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;

public class GithubRestReader {

    public static final String BAD_GITHUB_CREDENTIALS = "Bad Github credentials";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TOKEN = "token ";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_FORMAT = "application/vnd.github.v3+json";
    private static final String ERROR_MESSAGE = "Response HttpEntity was null";
    private final transient GithubConf githubConf;
    private final transient CloseableHttpClient httpClient;

    public GithubRestReader(CloseableHttpClient httpClient, GithubConf githubConf) {
        this.httpClient = httpClient;
        this.githubConf = githubConf;
    }

    public String readRest(String url) throws IOException, UnauthorizedException {
        HttpGet get = new HttpGet(url);
        get.setHeader(new BasicHeader(AUTHORIZATION, TOKEN + githubConf.getOauth()));
        get.setHeader(new BasicHeader(ACCEPT, ACCEPT_FORMAT));
        CloseableHttpResponse response = this.httpClient.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = null;
        switch (statusCode) {
            case HttpStatus.SC_OK:
                responseString = handleSuccess(response);
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                handleUnauthorized();
                break;
        }

        return responseString;
    }

    private void handleUnauthorized() throws UnauthorizedException {
        throw new UnauthorizedException(BAD_GITHUB_CREDENTIALS);
    }

    private String handleSuccess(CloseableHttpResponse response) throws IOException {
        HttpEntity responseEntity = response.getEntity();
        return IoUtils.streamToString(responseEntity.getContent());
    }

    public GitInfo getGitInfo() {
        return githubConf;
    }
}
