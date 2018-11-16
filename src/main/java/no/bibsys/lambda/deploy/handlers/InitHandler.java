package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.apigateway.ApiExporter;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.swaggerhub.ApiDocumentationInfo;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;
import no.bibsys.lambda.templates.SwaggerHubEditor;
import no.bibsys.swaggerhub.SwaggerDriver;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class InitHandler extends SwaggerHubEditor {


    @Override
    public SimpleResponse processInput(DeployEvent input, String apiGatewayMessage, Context context)
        throws IOException, URISyntaxException {

        System.out.println("Lambda function started");
        initFields();

        ApiDocumentationInfo apiDocInfo = constructApiDocumentationInfo();
        CloudFormationConfigurable config = new CloudFormationConfigurable(repository, branch);
        Optional<String> jsonOpt = generateApiSpec(apiDocInfo, config);

        System.out.println(jsonOpt.toString());

        if(jsonOpt.isPresent()){
            String response = readTheUpdatedAPI(jsonOpt.get(), apiDocInfo);
            return new SimpleResponse(response);
        }
        else{
            return new SimpleResponse("No API found");
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

    private void executeUpdate(ApiDocumentationInfo publishApi, String json, SwaggerDriver swaggerDriver)
        throws URISyntaxException, IOException {
        HttpPost request = swaggerDriver
            .createUpdateRequest(json, publishApi.getApiVersion());
        swaggerDriver.executePost(request);
    }

    private Optional<String> generateApiSpec(ApiDocumentationInfo publishAPi, CloudFormationConfigurable config)
        throws IOException {
        ApiExporter apiExporter = new ApiExporter(config, publishAPi.getStage());
        return apiExporter.generateOpenApiNoExtensions();

    }








}
