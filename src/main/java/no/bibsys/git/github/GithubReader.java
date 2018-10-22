package no.bibsys.git.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import no.bibsys.utils.JsonUtils;

public class GithubReader {


    private final transient static String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";


    private final transient GithubConf githubConf;
    private final RestReader restReader;


    private final transient String branch;

    public GithubReader(RestReader restReader, String branch) {
        this.githubConf = restReader.getGithubConf();
        this.branch = branch;
        this.restReader = restReader;
    }


    private String createUrl(Path path) {
        String pathString = path.toString();
        return String.format(urlTemplate,
            githubConf.getOwner(),
            githubConf.getRepo(),
            branch,
            pathString
        );

    }


    public String readFile(Path path) throws IOException {
        String downloadField = "download_url";
        String url = createUrl(path);
        String fileDescription = restReader.readRest(url);
        ObjectMapper parser = JsonUtils.newJsonParser();
        JsonNode rootNode = parser.readTree(fileDescription);
        if (rootNode.has(downloadField)) {
            String downloadUrl = rootNode.get(downloadField).asText();
            return restReader.readRest(downloadUrl);
        } else {
            throw new IllegalStateException("Could not find file " + path.toString());
        }


    }


    public String getBranch() {
        return branch;
    }

    public GithubConf getGithubConf() {
        return githubConf;
    }


}
