package no.bibsys.aws.utils.github;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.secrets.SecretsReader;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

public class GithubReaderTest {

    private static final String OWNER = "ownername";
    private static final String REPO = "reponame";
    private static final String BRANCH = "specialbranch";
    private final transient static String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";
    private static final SecretsReader SECRETS_READER = () -> "secret";
    private static final GithubConf githubConf = new GithubConf(OWNER, REPO, BRANCH,
        SECRETS_READER);
    public static final Path ARBITRARY_PATH = Paths.get("folder", "folder", "file");

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
        GithubReader githubReader = new GithubReader(githubConf, httpClient);
        String expectedString = "File contents";
        assertThat(githubReader.readFile(ARBITRARY_PATH), is(equalTo(expectedString)));
    }
}
