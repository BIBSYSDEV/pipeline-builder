package no.bibsys.git.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import no.bibsys.utils.IoUtils;
import no.bibsys.utils.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

public class GithubReader {

    private final transient static String urlTemplate = "https://api.github.com/"
        + "repos/%1$s/%2$s/contents/%4$s?ref=%3$s";
    private final transient IoUtils ioUtils = new IoUtils();
    private final transient GithubConf githubConf;
    private final transient String branch;

    public GithubReader(GithubConf githubConf, String branch) {
        this.githubConf = githubConf;
        this.branch = branch;
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
        String downloadField="download_url";
        String fileDescription = readRest(createUrl(path));
        ObjectMapper parser = JsonUtils.newJsonParser();
        JsonNode rootNode = parser.readTree(fileDescription);
        if(rootNode.has(downloadField)){
            String downloadUrl = rootNode.get(downloadField).asText();
            return readRest(downloadUrl);
        }
        else{
            throw new IllegalStateException("Could not file "+path.toString());
        }


    }


    private String readRest(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createMinimal();
        HttpGet get = new HttpGet(url);
        get.setHeader(new BasicHeader("Authentication", githubConf.getOauth()));
        get.setHeader(new BasicHeader("Accept", "application/vnd.github.v3+json"));
        CloseableHttpResponse response = httpClient.execute(get);
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            String responseString = ioUtils.streamToString(responseEntity.getContent());
            response.close();
            return responseString;

        } else {
            throw new IllegalStateException("Response HttpEntity was null");
        }

    }


}
