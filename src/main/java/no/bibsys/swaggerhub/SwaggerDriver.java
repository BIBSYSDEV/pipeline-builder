package no.bibsys.swaggerhub;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
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
        this.organization = organization;
        this.apiId = apiId;

    }



    public int executeUpdate(HttpUriRequest request) throws IOException {
        CloseableHttpClient client = newRestClient();
        CloseableHttpResponse response = client.execute(request);
        return response.getStatusLine().getStatusCode();
    }


    public int executeDelete(HttpDelete delete) throws IOException {
        return executeUpdate(delete);
    }

    public int executePost(HttpPost post) throws IOException {
        return executeUpdate(post);
    }


    public HttpPost createUpdateSpecificationPostRequest(String jsonSpec, String apiVersion)
        throws URISyntaxException {
        Optional<URI> uriOpt = urlFormater(
            apiUri(null),
            setupRequestParametersForUpdate(apiVersion));
        Optional<HttpPost> postOpt = uriOpt.map(uri -> createPostRequest(uri, jsonSpec));
        if(postOpt.isPresent())
            return  postOpt.get();
        else
            throw new IllegalStateException("Failed to create UpdatePost request.");
    }




    public HttpDelete createDeleteSpecificationVesionRequest(String apiVersion )
        throws URISyntaxException {

        Optional<URI> uriOpt = urlFormater(apiUri(apiVersion), Collections.emptyMap());
        Optional<HttpDelete> delete = uriOpt.map(uri -> new HttpDelete(uri));
        if(delete.isPresent())
            return delete.get();
        else{
            throw  new IllegalStateException("Failed to create DeletePost request");
        }

    }




    private Optional<URI> urlFormater(URI apiAddress,Map<String,String> requestParameters) {

        Optional<String> parameterOpt = joinParametersToString(requestParameters);
        String host = apiAddress.toString().replaceAll("/$", "");
        if(parameterOpt.isPresent()) {

            Optional<URI> uri = parameterOpt
                .map(parameterString -> String.join("?", host, parameterString))
                .map(URI::create);
            return uri;
        }
        else{
            return Optional.of(URI.create(host));
        }


    }

    private URI apiUri(String version) throws  URISyntaxException {
        if(version!=null && version.length()>0){
            return new URI(String.format("https://api.swaggerhub.com/apis/%s/%s/%s", organization, apiId,version));
        }
        else{
            return new URI(String.format("https://api.swaggerhub.com/apis/%s/%s", organization, apiId));
        }

    }

    private Optional<String> joinParametersToString(Map<String, String> parameters) {
        return parameters.entrySet()
            .stream()
            .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
            .reduce((str1, str2) -> String.join("&", str1, str2));
    }

    private Map<String, String> setupRequestParametersForUpdate(String version) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("isPrivate", "true");
        parameters.put("force", "true");
        parameters.put("version", version);
        return parameters;
    }



    private HttpPost createPostRequest(URI uri, String jsonSpec) {
        HttpPost post = new HttpPost();
        post.setURI(uri);
        addHeaders(post);
        addBody(post, jsonSpec);
        return post;

    }

    private void addHeaders(HttpPost post) {
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
