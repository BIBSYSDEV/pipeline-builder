package no.bibsys.aws.utils.github;

import java.io.IOException;
import java.nio.file.Path;
import no.bibsys.aws.git.github.GithubConf;
import org.apache.http.impl.client.CloseableHttpClient;

public class GithubReader {

    private final transient static String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";

    private final transient GithubConf githubConf;
    private final transient GithubRestReader githubRestReader;

    public GithubReader(GithubConf githubConf, CloseableHttpClient closeableHttpClient) {
        this.githubConf = githubConf;
        this.githubRestReader = new GithubRestReader(closeableHttpClient, githubConf);
    }

    public String createUrl(Path path) {
        String pathString = path.toString();
        return String.format(urlTemplate,
            this.githubConf.getOwner(),
            this.githubConf.getRepository(),
            this.githubConf.getBranch(),
            pathString
        );
    }

    public String readFile(Path filePath) throws UnauthorizedException, IOException, NotFoundException {
        return this.githubRestReader.readRest(filePath.toString());
    }
}
