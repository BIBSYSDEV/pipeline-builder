package no.bibsys.lambda.deploy.handlers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.apigateway.ApiGatewayApiInfo;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.lambda.deploy.handlers.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.swaggerhub.ApiDocumentationInfo;
import no.bibsys.lambda.responses.SimpleResponse;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.swaggerhub.SwaggerDriver;
import no.bibsys.utils.Environment;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class SwaggerHubUpdater {


    private final transient Environment environment = new Environment();

    protected transient String repository;
    protected transient String branch;
    protected transient String swaggerOrganization;
    protected transient String swaggerHubApiKey;
    protected transient String apiId;
    protected transient String apiVersion;
    protected transient String stage;
    protected transient String owner;


    public SwaggerHubUpdater() throws IOException {
        initFields();
    }


    protected void initFields() throws IOException {
        this.apiId = environment.readEnv("API_ID");
        this.apiVersion = environment.readEnv("API_VERSION");

        this.owner = environment.readEnv("OWNER");
        this.branch = environment.readEnv("BRANCH");
        this.repository = environment.readEnv("REPOSITORY");
        this.stage = environment.readEnv("STAGE");
        this.swaggerOrganization = environment.readEnv("SWAGGER_ORG");

        SecretsReader secretsReader = new SecretsReader();
        this.swaggerHubApiKey = secretsReader.readAuthFromSecrets("swaggerapikey", "swaggerapikey");

    }


    protected ApiDocumentationInfo constructApiDocumentationInfo() {
        ApiDocumentationInfo publishApi = new ApiDocumentationInfo();
        publishApi.setApiId(apiId);
        publishApi.setApiVersion(apiVersion);
        publishApi.setStage(stage);
        publishApi.setSwaggerOrganization(swaggerOrganization);
        publishApi.setSwaggetHubApiKey(swaggerHubApiKey);

        return publishApi;
    }


    protected Optional<String> updateApiDocumentation() throws IOException, URISyntaxException {
        ApiDocumentationInfo apiDocInfo = constructApiDocumentationInfo();
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

    private String readTheUpdatedAPI(String json, ApiDocumentationInfo publishApi)
        throws URISyntaxException, IOException {
        SwaggerDriver swaggerDriver = newSwaggerDriver(publishApi);
        executeUpdate(publishApi, json, swaggerDriver);
        HttpGet getSpecRequest = swaggerDriver
            .getSpecificationVersionRequest(publishApi.getApiVersion());
        return swaggerDriver.executeGet(getSpecRequest);
    }





    private SwaggerDriver newSwaggerDriver(ApiDocumentationInfo publishApi) {
        return new SwaggerDriver(publishApi.getSwaggetHubApiKey(),
            publishApi.getSwaggerOrganization(), publishApi.getApiId());
    }

    private void executeUpdate(ApiDocumentationInfo publishApi, String json,
        SwaggerDriver swaggerDriver)
        throws URISyntaxException, IOException {
        HttpPost request = swaggerDriver
            .createUpdateRequest(json, publishApi.getApiVersion());
        swaggerDriver.executePost(request);
    }

    private Optional<String> generateApiSpec(ApiDocumentationInfo publishAPi,
        CloudFormationConfigurable config)
        throws IOException {
        ApiGatewayApiInfo apiGatewayApiInfo = new ApiGatewayApiInfo(config, publishAPi.getStage());
        return apiGatewayApiInfo.generateOpenApiNoExtensions();

    }


}
