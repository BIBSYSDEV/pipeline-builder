package no.bibsys.aws.utils.github;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.secrets.AwsSecretsReader;
import no.bibsys.aws.secrets.SecretsReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Test;

public class GithubReaderTest extends GithubTestUtilities {

    public static final Path ARBITRARY_PATH = Paths.get("folder", "folder", "file");
    private static final String OWNER = "ownername";
    private static final String REPO = "reponame";
    private static final String BRANCH = "specialbranch";
    private final transient static String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";
    private static final SecretsReader SECRETS_READER = () -> "secret";
    private static final GithubConf githubConf = new GithubConf(OWNER, REPO, BRANCH,
        SECRETS_READER);

    @Test
    public void createUrlFromPathShouldMapPathToValidUrl() {

        Path inputPath = Paths.get("directory/directory/file");
        String expectedUrl = String.format(urlTemplate, OWNER, REPO, BRANCH, inputPath);
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);

        GithubReader githubReader = new GithubReader(githubConf, httpClient);
        String outputUrl = githubReader.createUrl(inputPath);
        assertThat(outputUrl, is(equalTo(expectedUrl)));
    }

    @Test
    public void readFileShouldReturnAFileForAValidPath() throws IOException, UnauthorizedException, NotFoundException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);

        when(httpClient.execute(any())).thenReturn(response);
        when(response.getEntity()).thenReturn(SIMPLE_RESPONSE);
        when(response.getStatusLine()).thenReturn(STATUS_LINE_OK);
        GithubReader githubReader = new GithubReader(githubConf, httpClient);
        assertThat(githubReader.readFile(ARBITRARY_PATH), is(equalTo(EXPECTED_RESPONSE)));
    }

    @Test
    public void readFileFromGithub() throws IOException, UnauthorizedException, NotFoundException {
        CloseableHttpClient client = HttpClients.createMinimal();
        AwsSecretsReader secretsReader = new AwsSecretsReader(AWSSecretsManagerClientBuilder.defaultClient(),
            "infrastructure", "read_from_github");
        GithubConf githubConf = new GithubConf("BIBSYSDEV", "pipeline-builder", "master", secretsReader);
        GithubReader githubReader = new GithubReader(githubConf, client);
        String contents = githubReader.readFile(Paths.get("template.yml"));
        System.out.println(contents);
    }
}
