package no.bibsys.aws.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.aws.apigateway.ApiGatewayApiInfo;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.swaggerhub.SwaggerDriver;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates the OpenApi specification stored in SwaggerHub for a specific ApiGateway API.
 */

public class SwaggerHubUpdater {


    private static final Logger logger = LoggerFactory.getLogger(SwaggerHubUpdater.class);
    private final transient SwaggerHubInfo swaggerHubInfo;
    private final transient AmazonApiGateway apiGateway;
    private final transient String swaggerApiKey;
    private final transient String apiGatewayRestApiId;
    protected transient Stage stage;

    public SwaggerHubUpdater(AmazonApiGateway apiGateway, String apiGatewayRestApiId, SwaggerHubInfo swaggerHubInfo,
            Stage stage) throws IOException {
        this.apiGateway = apiGateway;
        this.apiGatewayRestApiId = apiGatewayRestApiId;
        this.stage = stage;
        this.swaggerHubInfo = swaggerHubInfo;
        this.swaggerApiKey = swaggerHubInfo.getSwaggerAuth();
    }


    /**
     * Deletes the whole API documentation from SwaggerHub.
     *
     * @return Sucess of Failure code the delete request
     */
    public int deleteApi() throws URISyntaxException, IOException {
        SwaggerDriver swaggerDriver = new SwaggerDriver(swaggerHubInfo);
        HttpDelete deleteRequest = swaggerDriver.createDeleteApiRequest(swaggerApiKey);
        return swaggerDriver.executeDelete(deleteRequest);
    }

    /**
     * Updates the API documentation in SwaggerHub.
     *
     * @return The body of the HTTP response for the update query
     */

    public Optional<String> updateApiDocumentation() throws IOException, URISyntaxException {


        Optional<String> jsonOpt = generateApiSpec();

        logger.debug(jsonOpt.toString());

        if (jsonOpt.isPresent()) {
            logger.debug("Found json API");
            SwaggerDriver swaggerDriver = newSwaggerDriver();
            executeUpdate(jsonOpt.get(), swaggerDriver);
            String response = readTheUpdatedAPI(swaggerDriver);
            return Optional.of(response);
        } else {
            return Optional.empty();
        }

    }



    private String readTheUpdatedAPI(SwaggerDriver swaggerDriver) throws URISyntaxException, IOException {

        HttpGet getSpecRequest = swaggerDriver.getSpecificationRequest(swaggerHubInfo.getApiVersion());
        return swaggerDriver.executeGet(getSpecRequest);
    }


    private SwaggerDriver newSwaggerDriver() {
        return new SwaggerDriver(swaggerHubInfo);
    }

    private void executeUpdate(String json, SwaggerDriver swaggerDriver) throws URISyntaxException, IOException {
        HttpPost request = swaggerDriver.createUpdateRequest(json, swaggerHubInfo.getApiVersion(), swaggerApiKey);
        swaggerDriver.executePost(request);


    }

    private Optional<String> generateApiSpec() throws IOException {
        ApiGatewayApiInfo apiGatewayApiInfo = new ApiGatewayApiInfo(stage, apiGateway, apiGatewayRestApiId);
        return apiGatewayApiInfo.generateOpenApiNoExtensions();

    }


}
