package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.apigateway.ApiExporter;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.handler.requests.ApiDocumentationInfo;
import no.bibsys.handler.requests.buildevents.BuildEvent;
import no.bibsys.handler.responses.SimpleResponse;
import no.bibsys.handler.templates.SwaggerHubEditor;
import no.bibsys.swaggerhub.SwaggerDriver;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitHandler extends SwaggerHubEditor {

    private final static Logger logger = LoggerFactory.getLogger(InitHandler.class);

    @Override
    public SimpleResponse processInput(BuildEvent input, String apiGatewayMessage, Context context)
        throws IOException, URISyntaxException {

        logger.info("Lambda function started");
        initFields();

        ApiDocumentationInfo publishApi = newPublishApi();
        CloudFormationConfigurable config = new CloudFormationConfigurable(
            publishApi.getRepository()
            , publishApi.getBranch());
        Optional<String> jsonOpt = generateApiSpec(publishApi, config);

        logger.info(jsonOpt.toString());

        if(jsonOpt.isPresent()){
            String response = readTheUpdatedAPI(jsonOpt.get(),publishApi);
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
