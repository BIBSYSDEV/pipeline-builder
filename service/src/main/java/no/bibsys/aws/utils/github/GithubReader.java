package no.bibsys.aws.utils.github;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import no.bibsys.aws.git.github.GithubConf;
import org.apache.http.impl.client.CloseableHttpClient;

public class GithubReader {

    public static final String GITHUB_PATH_NOT_FOUND = "Github path not found:%s";
    protected static final String GITHUBCONF_NULL_ERROR_MESSAGE = "You need to set the githubConf";
    private static final transient String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";
    private final transient GithubRestReader githubRestReader;
    private transient GithubConf githubConf;

    public GithubReader(CloseableHttpClient closeableHttpClient) {
        this.githubRestReader = new GithubRestReader(closeableHttpClient);
    }

    public String createUrl(Path path) {
        Objects.requireNonNull(githubConf, GITHUBCONF_NULL_ERROR_MESSAGE);
        String pathString = path.toString();
        return String.format(urlTemplate,
            this.githubConf.getOwner(),
            this.githubConf.getRepository(),
            this.githubConf.getBranch(),
            pathString
        );
    }

    public String readFile(Path filePath) throws UnauthorizedException, IOException, NotFoundException {
        String url = createUrl(filePath);
        this.githubRestReader.setGitHubConf(this.githubConf);
        Optional<String> result = this.githubRestReader
            .executeRequest(githubRestReader.createRequest(url));
        return result
            .orElseThrow(() -> new NotFoundException(String.format(GITHUB_PATH_NOT_FOUND, url)));
    }

    public GithubReader setGitHubConf(GithubConf gitHubConf) {
        this.githubConf = gitHubConf;
        return this;
    }
}
