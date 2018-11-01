package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.GetExportRequest;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.RestApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class PipelineTest {

    private String branchName = "autreg-58-openapi-lambda";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";

    //   curl -X POST "https://api.swaggerhub.com/apis/owner/apiId?isPrivate=true&version=1.0&force=true"
    // -H  "accept: application/json" -H  "Authorization: 8-9477-d8f297f3d5a1" -H  "Content-Type: application/json" -d "{\"some\":\"json\"}"


    @Test
    @Category(DoNotRunTest.class)
    public void createStacks() throws IOException {
        Application application = initApplication();
        application.createStacks();
    }

    @Test
    @Category(DoNotRunTest.class)
    public void deleteStacks() throws IOException {
        Application application = initApplication();
        application.wipeStacks();

    }

    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, new Environment());
        GithubReader githubReader = new GithubReader(new RestReader(githubConf), branchName);
        return new Application(githubReader);
    }

    private JsonNode readYaml() throws IOException {
        InputStream input = Files.newInputStream(Paths.get("template.yml"));
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode node = mapper.readValue(input, JsonNode.class);
        return node;
    }

    private String apiName(JsonNode template) {
        return template.get("Resources").get("RestApi").get("Properties").get("Name").asText();
    }


    private String cutString(String inputString) {
        int maxLenght = Math.min(inputString.length(), 63);
        return inputString.substring(0, maxLenght);
    }


//    @Test
//    @Category(DoNotRunTest.class)
    public String generateOpenApi() throws IOException {
        Application application = initApplication();
        PipelineStackConfiguration config = application.getPipelineStackConfiguration();

        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        List<RestApi> apiList = apiGateway
            .getRestApis(new GetRestApisRequest().withLimit(100)).getItems();

        Optional<RestApi> apiOpt = apiList.stream()
            .filter(api -> api.getName().contains(config.getProjectId()))
            .filter(api -> api.getName().contains(config.getNormalizedBranchName()))
            .findFirst();

        if (apiOpt.isPresent()) {
            RestApi api = apiOpt.get();

            Map<String, String> requestParameters = new HashMap<>();
            requestParameters.put("extensions", "apigateway");
            requestParameters.put("accepts", "application/json");
            GetExportRequest request = new GetExportRequest().withRestApiId(api.getId())
                .withStageName("final").withExportType("oas30").withParameters(requestParameters);
            GetExportResult result = apiGateway
                .getExport(request);
            String swaggerFile = new String(result.getBody().array());
            return swaggerFile;

        }

        return null;


    }


    @Test
    @Category(DoNotRunTest.class)
    public void postApi() throws IOException {
        String jsonApi = generateOpenApi();
        CloseableHttpClient client = HttpClients.createMinimal();

        HttpPost post = new HttpPost();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("isPrivate", "true");
        parameters.put("force", "true");
        parameters.put("version", "1.0");
        URI uri = urlFormater("Unit3", "small-api", parameters).get();
        post.setURI(uri);
        post.addHeader("accept", "application/json");
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authorization", "");
        StringEntity stringEntity=new StringEntity(jsonApi, StandardCharsets.UTF_8);
        post.setEntity(stringEntity);

        CloseableHttpResponse response = client.execute(post);
        System.out.print(response);

//        StringEntity stringEntity = new StringEntity();

    }


    private Optional<URI> urlFormater(String organization, String apiId,
        Map<String, String> parameters) {
        String host = String.format("https://api.swaggerhub.com/apis/%s/%s", organization, apiId);
        StringBuffer builder = new StringBuffer();
        builder.append(host);
        Optional<String> paramteterOpt = parameters.entrySet()
            .stream()
            .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
            .reduce((str1, str2) -> String.join("&", str1, str2));
        Optional<URI> uri = paramteterOpt.map(p -> String.join("?", host, p))
            .map(URI::create);
        return uri;

    }


}
