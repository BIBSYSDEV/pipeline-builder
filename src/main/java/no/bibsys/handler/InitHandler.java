package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.apigateway.ApiExporter;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.handler.requests.PublishApi;
import no.bibsys.handler.requests.buildevents.BuildEvent;
import no.bibsys.handler.responses.SimpleResponse;
import no.bibsys.handler.templates.CodePipelineFunctionHandlerTemplate;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.swaggerhub.SwaggerDriver;
import no.bibsys.utils.Environment;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class InitHandler extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {

    private transient String repository;
    private transient String branch;
    private transient String swaggerOrganization;
    private transient String swaggerHubApiKey;
    private transient String apiId;
    private transient String apiVersion;
    private transient String stage;
    private transient String owner;

    private final transient Environment environment = new Environment();


    @Override
    public SimpleResponse processInput(BuildEvent input, Context context)
        throws IOException, URISyntaxException {

        System.out.println(input);
        System.out.println("Lambda function started");
        initFields(environment);
        PublishApi publishApi = newPublishApi();
        CloudFormationConfigurable config = new CloudFormationConfigurable(
            publishApi.getRepository()
            , publishApi.getBranch());
        Optional<String> jsonOpt = generateApiSpec(publishApi, config);
        System.out.println(jsonOpt.toString());
        if(jsonOpt.isPresent()){
            String json = jsonOpt.get();
            SwaggerDriver swaggerDriver = newSwaggerDriver(publishApi);
            executeUpdate(publishApi, json, swaggerDriver);
            String response = readTheUpdatedAPI(publishApi, swaggerDriver);
            return new SimpleResponse(response);
        }
        else{

            return new SimpleResponse("No API found");
        }


    }

    private String readTheUpdatedAPI(PublishApi publishApi, SwaggerDriver swaggerDriver)
        throws URISyntaxException, IOException {
        HttpGet getSpecRequest = swaggerDriver
            .getSpecificationVesionRequest(publishApi.getApiVersion());
        return swaggerDriver.executeGet(getSpecRequest);
    }

    private SwaggerDriver newSwaggerDriver(PublishApi publishApi) {
        return new SwaggerDriver(publishApi.getSwaggetHubApiKey(),
                    publishApi.getSwaggerOrganization(), publishApi.getApiId());
    }

    private void executeUpdate(PublishApi publishApi, String json, SwaggerDriver swaggerDriver)
        throws URISyntaxException, IOException {
        HttpPost request = swaggerDriver
            .updateSpecificationPostRequest(json, publishApi.getApiVersion());
        swaggerDriver.executeUpdate(request);
    }

    private Optional<String> generateApiSpec(PublishApi input, CloudFormationConfigurable config)
        throws IOException {
        ApiExporter apiExporter = new ApiExporter(config, input.getStage());
        return apiExporter.generateOpenApiNoExtensions();

    }


    private void initFields(Environment environment) throws IOException {
        this.apiId = environment.readEnvOpt("API_ID").orElseThrow(() -> newException("API_ID"));
        this.apiVersion = environment.readEnvOpt("API_VERSION")
            .orElseThrow(() -> newException("API_VERSION"));
        this.owner = environment.readEnvOpt("OWNER").orElseThrow(() -> newException("OWNER"));
        this.branch = environment.readEnvOpt("BRANCH").orElseThrow(() -> newException("BRANCH"));
        this.repository = environment.readEnvOpt("REPOSITORY")
            .orElseThrow(() -> newException("REPOSITORY"));
        this.stage = environment.readEnvOpt("STAGE").orElseThrow(() -> newException("STAGE"));
        this.swaggerOrganization = environment.readEnvOpt("SWAGGER_ORG")
            .orElseThrow(() -> newException("SWAGGER_ORG"));

        SecretsReader secretsReader = new SecretsReader();
        this.swaggerHubApiKey = secretsReader.readAuthFromSecrets("swaggerapikey", "swaggerapikey");

    }

    private IllegalStateException newException(String missingVariable) {
        return new IllegalStateException(String.format("%s is missing", missingVariable));

    }


    private PublishApi newPublishApi() {
        PublishApi publishApi = new PublishApi();
        publishApi.setApiId(apiId);
        publishApi.setApiVersion(apiVersion);
        publishApi.setStage(stage);
        publishApi.setSwaggerOrganization(swaggerOrganization);
        publishApi.setSwaggetHubApiKey(swaggerHubApiKey);
        publishApi.setBranch(branch);
        publishApi.setOwner(owner);
        publishApi.setRepository(repository);
        return publishApi;
    }
}
