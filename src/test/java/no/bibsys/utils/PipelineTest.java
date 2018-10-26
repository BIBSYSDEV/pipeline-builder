package no.bibsys.utils;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.GetExportRequest;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.RestApi;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.SetBucketPolicyRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.bibsys.Application;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {

    private String branchName = "autreg-58-openapi-lambda";
    private String repoName = "authority-registry-infrastructure";
    private String repoOwner = "BIBSYSDEV";

/* response = api_gateway_client.get_export(restApiId=os.environ.get('REST_API_ID'),
                              stageName=os.environ.get('STAGE_NAME'),
                              exportType='oas30',
                              parameters={'extensions':'apigateway'},
                              accepts='application/json')*/

/*Type: AWS::S3::Bucket
    Properties:
      BucketName: !Join ['-', [!Ref 'ProjectId', !Ref Branch, !Ref 'Stage', "swagger-ui"]]
      AccessControl: PublicRead
      WebsiteConfiguration:
        IndexDocument: 'index.html'
      CorsConfiguration:
        CorsRules:
        - AllowedHeaders:
          - '*'
          AllowedMethods:
          - GET
          - PUT
          - HEAD
          - POST
          - DELETE
          AllowedOrigins:
          - '*'*/


//    @Test
//    public void postApi() throws IOException {
//        String jsonApi="lalala";
//        CloseableHttpClient client = HttpClients.createMinimal();
//
//        HttpPost post = new HttpPost();
//        URI uri=URI.create("https://api.swaggerhub.com/apis/Unit3/testingAPI");
//        MultipartEntityBuilder.create();
//        post.setURI());
//
//
//        ArrayList<NameValuePair> parameters = new ArrayList<>();
//        parameters.add(new BasicNameValuePair("isPrivate", "false"));
//        parameters.add(new BasicNameValuePair("force", "true"));
//        parameters.add(new BasicNameValuePair("version", "1.0"));
//        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
//
//        post.setParams();
//
//
//
//        post.addHeader("accept", "application/json");
//        post.addHeader("Content-Type", "application/json");
//
//        String content=IoUtils.streamToString(entity.getContent());
//
//        System.out.print(content);
//
//    }

    @Test
    @Ignore
    public void createStacks() throws IOException {
        Application application = initApplication();
        application.createStacks();
    }

    @Test
    @Ignore
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

    @Test
    @Ignore
    public void createSwaggerUIBacket()
        throws IOException {
        Application application = initApplication();
        PipelineStackConfiguration config = application.getPipelineStackConfiguration();
        String serviceStackName = config.getPipelineConfiguration().getFinalServiceStack();

        String bucketName = cutString(String.format("swagger-%s", serviceStackName));
        AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        s3.deleteBucket(bucketName);
        Bucket bucket = s3.createBucket(bucketName);
        String bucketPolicy = IoUtils
            .resourceAsString(Paths.get("policies", "publicAccessBucket.json"));
        bucketPolicy = bucketPolicy.replaceAll("BUCKETNAME", bucketName);
        s3.setBucketPolicy(new SetBucketPolicyRequest(bucketName, bucketPolicy));


    }

    private void downloadFile() {
        String version = "3.19.3";
        String url = "https://github.com/swagger-api/swagger-ui/archive/v" + version + ".tar.gz";

    }

    private void getFile(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createMinimal();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(get);
        InputStream inputStream = response.getEntity().getContent();

    }

    private String cutString(String inputString) {
        int maxLenght = Math.min(inputString.length(), 63);
        return inputString.substring(0, maxLenght);
    }

    @Test
    @Ignore
    public void generateOpenApi() throws IOException {
        Application application = initApplication();
        PipelineStackConfiguration config = application.getPipelineStackConfiguration();

        String apiName = apiName(readYaml());
        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        Optional<RestApi> apiOpt = apiGateway
            .getRestApis(new GetRestApisRequest().withLimit(100)).getItems()
            .stream().filter(item -> item.getName().equals(apiName))
            .findFirst();

        if (apiOpt.isPresent()) {
            RestApi api = apiOpt.get();

            Map<String, String> requestParameters = new HashMap<>();
            requestParameters.put("extensions", "apigateway");
            requestParameters.put("accepts", "application/json");
            GetExportRequest request = new GetExportRequest().withRestApiId(api.getId())
                .withStageName("test").withExportType("oas30").withParameters(requestParameters);
            GetExportResult result = apiGateway
                .getExport(request);
            String swaggerFile = new String(result.getBody().array());
            System.out.println(swaggerFile);


        }


    }
    /*curl -X POST "https://api.swaggerhub.com/apis/OWNER/theAPI?isPrivate=false&version=1.0&force=true" -H  "accept: application/json" -H  "Content-Type: application/json" -d "BBOOODYYYY"*/

}
