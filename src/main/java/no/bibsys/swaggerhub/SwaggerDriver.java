package no.bibsys.swaggerhub;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.utils.IoUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SwaggerDriver {

    private final transient String apiKey;
    private final transient String organization;
    private final transient String apiId;


    public SwaggerDriver(String apiKey, String organization, String apiId) {
        this.apiKey = apiKey;
        this.organization=organization;
        this.apiId=apiId;


    }


    public String executeGet(HttpGet get) throws IOException {
        CloseableHttpClient client = newRestClient();
        CloseableHttpResponse response = client.execute(get);
        String output = IoUtils.streamToString(response.getEntity().getContent());
        return output;
    }


    public HttpGet getSpecificationVersionRequest(String apiVersion)
        throws URISyntaxException {
        SwaggerHubUrlFormatter swaggerHubUrlFormatter =new SwaggerHubUrlFormatter(organization,
            apiId,apiVersion,Collections.emptyMap());


        HttpGet httpGet =createGetRequest(swaggerHubUrlFormatter) ;
        return httpGet;

    }


    public int executeDelete(HttpDelete delete) throws IOException {
        return executeUpdate(delete);
    }

    public int executePost(HttpPost post) throws IOException {
        return executeUpdate(post);
    }

    public HttpPost createUpdateRequest(String jsonSpec, String apiVersion)
        throws URISyntaxException {

        Map<String, String> parameters = setupRequestParametersForUpdate(apiVersion);
        SwaggerHubUrlFormatter formatter=
            new SwaggerHubUrlFormatter(organization,apiId,null,parameters);
        HttpPost postOpt = createPostRequest(formatter, jsonSpec);
        return postOpt;


    }


    private int executeUpdate(HttpUriRequest request) throws IOException {
        CloseableHttpClient client = newRestClient();
        CloseableHttpResponse response = client.execute(request);
        return response.getStatusLine().getStatusCode();
    }


    public HttpDelete createDeleteApiRequest() throws URISyntaxException {
        SwaggerHubUrlFormatter formatter=new SwaggerHubUrlFormatter(
            organization,
            apiId,
            null,
            Collections.emptyMap());
        HttpDelete delete=createDeleteRequest(formatter);
        return delete;
    }


    private HttpPost createPostRequest(SwaggerHubUrlFormatter formatter, String jsonSpec) {
        HttpPost post = new HttpPost();
        post.setURI(formatter.getRequestURL());
        addHeaders(post);
        addBody(post, jsonSpec);
        return post;

    }

    private HttpDelete createDeleteRequest(SwaggerHubUrlFormatter formatter) {
        HttpDelete delete = new HttpDelete(formatter.getRequestURL());
        addHeaders(delete);
        return delete;
    }

    private HttpGet createGetRequest(SwaggerHubUrlFormatter swaggerHubUrlFormatter) {
        URI uri = swaggerHubUrlFormatter.getRequestURL();
        HttpGet get = new HttpGet(uri);
        addHeaders(get);
        return get;
    }






    public HttpDelete createDeleteVersionRequest(String apiVersion)
        throws URISyntaxException {

       SwaggerHubUrlFormatter formatter=new SwaggerHubUrlFormatter(organization,
           apiId,apiVersion,Collections.emptyMap());
        HttpDelete delete = createDeleteRequest(formatter);
        return delete;

    }




    private Map<String, String> setupRequestParametersForUpdate(String version) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("isPrivate", "false");
        parameters.put("force", "true");
        parameters.put("version", version);
        return parameters;
    }


    private void addHeaders(HttpUriRequest post) {
        post.addHeader("accept", "application/json");
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authorization", apiKey);

    }

    private void addBody(HttpPost post, String jsonSpec) {
        StringEntity stringEntity = new StringEntity(jsonSpec, StandardCharsets.UTF_8);
        post.setEntity(stringEntity);
    }


    private CloseableHttpClient newRestClient() {
        return HttpClients.createMinimal();
    }




}
