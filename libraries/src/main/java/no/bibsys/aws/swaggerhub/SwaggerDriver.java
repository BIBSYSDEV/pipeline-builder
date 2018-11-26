package no.bibsys.aws.swaggerhub;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.bibsys.aws.tools.IoUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerDriver {


    private final static Logger logger = LoggerFactory.getLogger(SwaggerDriver.class);
    private final transient SwaggerHubInfo swaggerHubInfo;


    public SwaggerDriver(SwaggerHubInfo swaggerHubInfo)  {
        this.swaggerHubInfo=swaggerHubInfo;


    }


    public String executeGet(HttpGet get) throws IOException {
        CloseableHttpClient client = newRestClient();
        CloseableHttpResponse response = client.execute(get);
        String output = IoUtils.streamToString(response.getEntity().getContent());
        return output;
    }


    /**
     * It retrieves the OpenAPI specification stored in SwaggerHub for the API specified in the
     * {@code swaggerHubInfo} field.
     *
     * @return The OpenAPI specification stored in SwaggerHub for a specific API.
     */
    public HttpGet getSpecificationRequest(String apiKey)
        throws URISyntaxException {
        SwaggerHubUrlFormatter swaggerHubUrlFormatter =new SwaggerHubUrlFormatter(swaggerHubInfo,
            true,
            Collections.emptyMap());
        HttpGet httpGet =createGetRequest(swaggerHubUrlFormatter,apiKey) ;
        return httpGet;

    }


    public int executeDelete(HttpDelete delete) throws IOException {
        return executeUpdate(delete);
    }

    public int executePost(HttpPost post) throws IOException {
        return executeUpdate(post);
    }

    public HttpPost createUpdateRequest(String jsonSpec, String apiVersion, String apiKey)
        throws URISyntaxException {

        Map<String, String> parameters = setupRequestParametersForUpdate(apiVersion);
        SwaggerHubUrlFormatter formatter=
            new SwaggerHubUrlFormatter(swaggerHubInfo,false,parameters);
        HttpPost postOpt = createPostRequest(formatter, jsonSpec,apiKey);
        return postOpt;


    }


    private int executeUpdate(HttpUriRequest request) throws IOException {
        CloseableHttpClient client = newRestClient();
        CloseableHttpResponse response = client.execute(request);
        int result = response.getStatusLine().getStatusCode();
        if (logger.isInfoEnabled()) {
            logger.info("SwaggerHubUpdateResultCode:{}", result);
        }

        return result;
    }


    public HttpDelete createDeleteApiRequest(String apiKey) throws URISyntaxException {
        SwaggerHubUrlFormatter formatter=new SwaggerHubUrlFormatter(
            swaggerHubInfo,false,Collections.emptyMap());
        HttpDelete delete=createDeleteRequest(formatter,apiKey);
        return delete;
    }


    private HttpPost createPostRequest(SwaggerHubUrlFormatter formatter, String jsonSpec,String apiKey) {
        HttpPost post = new HttpPost();
        post.setURI(formatter.getRequestURL());
        addHeaders(post,apiKey);
        addBody(post, jsonSpec);
        return post;

    }

    private HttpDelete createDeleteRequest(SwaggerHubUrlFormatter formatter,String apiKey) {
        HttpDelete delete = new HttpDelete(formatter.getRequestURL());
        addHeaders(delete,apiKey);
        return delete;
    }

    private HttpGet createGetRequest(SwaggerHubUrlFormatter swaggerHubUrlFormatter,String apiKey) {
        URI uri = swaggerHubUrlFormatter.getRequestURL();
        HttpGet get = new HttpGet(uri);
        addHeaders(get,apiKey);
        return get;
    }






    public HttpDelete createDeleteVersionRequest(String apiKey)
        throws URISyntaxException {

       SwaggerHubUrlFormatter formatter=new SwaggerHubUrlFormatter(swaggerHubInfo,true,Collections.emptyMap());
        HttpDelete delete = createDeleteRequest(formatter,apiKey);
        return delete;

    }




    private Map<String, String> setupRequestParametersForUpdate(String version) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("isPrivate", "false");
        parameters.put("force", "true");
        parameters.put("version", version);
        return parameters;
    }


    private void addHeaders(HttpUriRequest post,String apiKey) {
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
