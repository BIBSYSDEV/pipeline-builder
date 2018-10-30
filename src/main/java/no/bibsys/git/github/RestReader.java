package no.bibsys.git.github;

import java.io.IOException;
import no.bibsys.utils.IoUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

public class RestReader {


    private final transient GithubConf githubConf;


    public RestReader(GithubConf githubConf) {
        this.githubConf = githubConf;

    }

    public String readRest(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createMinimal();
        HttpGet get = new HttpGet(url);
        get.setHeader(new BasicHeader("Authentication", "token " + githubConf.getOauth()));
        get.setHeader(new BasicHeader("Accept", "application/vnd.github.v3+json"));
        CloseableHttpResponse response = httpClient.execute(get);
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {
            String responseString = IoUtils.streamToString(responseEntity.getContent());
            response.close();
            return responseString;

        } else {
            throw new IllegalStateException("Response HttpEntity was null");
        }

    }


    public GithubConf getGithubConf() {
        return githubConf;
    }
}
