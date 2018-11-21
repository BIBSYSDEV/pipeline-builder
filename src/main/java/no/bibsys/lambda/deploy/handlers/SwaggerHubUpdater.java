package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.apigateway.ApiGatewayApiInfo;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.cloudformation.Stage;
import no.bibsys.swaggerhub.ApiDocumentationInfo;
import no.bibsys.swaggerhub.SwaggerDriver;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class SwaggerHubUpdater {

    protected final transient String repository;
    protected final transient String branch;

    private final transient SwaggerHubInfo swaggerHubInfo;
    private final transient AmazonApiGateway apiGateway;
    private final transient String swaggerApiKey;
    protected transient Stage stage;


    public SwaggerHubUpdater(AmazonApiGateway apiGateway,
        SwaggerHubInfo swaggerHubInfo,
        String repository,
        String branch,
        Stage stage
    ) throws IOException {
        this.apiGateway = apiGateway;
        this.branch = branch;
        this.repository = repository;
        this.stage = stage;
        this.swaggerHubInfo = swaggerHubInfo;
        this.swaggerApiKey=swaggerHubInfo.getSwaggerAuth();
    }


    public int deleteApi() throws URISyntaxException, IOException {
        SwaggerDriver swaggerDriver = new SwaggerDriver(swaggerHubInfo);
        HttpDelete deleteRequest = swaggerDriver
            .createDeleteApiRequest(swaggerApiKey);
        return swaggerDriver.executeDelete(deleteRequest);
    }


    public Optional<String> updateApiDocumentation()
        throws IOException, URISyntaxException {
        ApiDocumentationInfo apiDocInfo = constructApiDocumentationInfo(swaggerHubInfo.getApiVersion());
        CloudFormationConfigurable config = new CloudFormationConfigurable(repository, branch);
        Optional<String> jsonOpt = generateApiSpec(apiDocInfo, config);

        System.out.println(jsonOpt.toString());

        if (jsonOpt.isPresent()) {
            String response = readTheUpdatedAPI(jsonOpt.get(), apiDocInfo);
            return Optional.of(response);
        } else {
            return Optional.empty();
        }

    }


    private ApiDocumentationInfo constructApiDocumentationInfo(String apiVersion) {
        ApiDocumentationInfo publishApi = new ApiDocumentationInfo();
        publishApi.setApiId(swaggerHubInfo.getApiId());
        publishApi.setApiVersion(apiVersion);
        publishApi.setStage(stage.toString());
        publishApi.setSwaggerOrganization(swaggerHubInfo.getSwaggerOrganization());
        publishApi.setSwaggetHubApiKey(swaggerApiKey);

        return publishApi;
    }


    private String readTheUpdatedAPI(String json, ApiDocumentationInfo publishApi)
        throws URISyntaxException, IOException {
        SwaggerDriver swaggerDriver = newSwaggerDriver();
        executeUpdate(publishApi, json, swaggerDriver);
        HttpGet getSpecRequest = swaggerDriver
            .getSpecificationVersionRequest(publishApi.getApiVersion());
        return swaggerDriver.executeGet(getSpecRequest);
    }


    private SwaggerDriver newSwaggerDriver()  {
        return new SwaggerDriver(swaggerHubInfo);
    }

    private void executeUpdate(ApiDocumentationInfo publishApi, String json,
        SwaggerDriver swaggerDriver)
        throws URISyntaxException, IOException {
        HttpPost request = swaggerDriver
            .createUpdateRequest(json, publishApi.getApiVersion(), swaggerApiKey);
        swaggerDriver.executePost(request);
    }

    private Optional<String> generateApiSpec(ApiDocumentationInfo publishAPi,
        CloudFormationConfigurable config)
        throws IOException {
        ApiGatewayApiInfo apiGatewayApiInfo = new ApiGatewayApiInfo(config, publishAPi.getStage(),
            apiGateway);
        return apiGatewayApiInfo.generateOpenApiNoExtensions();

    }


}
