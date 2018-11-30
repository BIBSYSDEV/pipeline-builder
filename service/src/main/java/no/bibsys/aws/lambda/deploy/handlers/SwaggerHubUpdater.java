package no.bibsys.aws.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.aws.apigateway.ApiGatewayApiInfo;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.swaggerhub.SwaggerDriver;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.utils.constants.GitConstants;
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

    public SwaggerHubUpdater(
        AmazonApiGateway apiGateway,
        String apiGatewayRestApiId,
        SwaggerHubInfo swaggerHubInfo,
        Stage stage,
        String stackName,
        GitInfo gitInfo) throws IOException {
        this.apiGateway = apiGateway;
        this.apiGatewayRestApiId = apiGatewayRestApiId;
        this.stage = stage;
        this.swaggerHubInfo = intializeSwaggerHubInfo(swaggerHubInfo,gitInfo,stackName);
        this.swaggerApiKey = swaggerHubInfo.getSwaggerAuth();
    }

    private SwaggerHubInfo intializeSwaggerHubInfo(SwaggerHubInfo swaggerHubInfo, GitInfo gitInfo,String stackName) {
        String branch=gitInfo.getBranch();
        if(branch.equalsIgnoreCase(GitConstants.MASTER)){
            return swaggerHubInfo;
        }
        else{
            String org=swaggerHubInfo.getSwaggerOrganization();
            String version=swaggerHubInfo.getApiVersion();
            return new SwaggerHubInfo(stackName,version,org);
        }

    }


    public int deleteApiVersion() throws URISyntaxException, IOException {
        SwaggerDriver swaggerDriver=new SwaggerDriver(swaggerHubInfo);
        HttpDelete deleteRequest = swaggerDriver.createDeleteVersionRequest(swaggerApiKey);
        int result=swaggerDriver.executeDelete(deleteRequest);
        return result;
    }




    /**
     * Deletes the whole API documentation from SwaggerHub.
     *
     * @return Success of Failure code the delete request
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

        HttpGet getSpecRequest = swaggerDriver.getSpecificationRequest(swaggerApiKey);
        return swaggerDriver.executeGet(getSpecRequest);
    }


    private SwaggerDriver newSwaggerDriver() {
        return new SwaggerDriver(swaggerHubInfo);
    }

    private void executeUpdate(String json, SwaggerDriver swaggerDriver) throws URISyntaxException, IOException {
        HttpPost request = swaggerDriver.createUpdateRequest(json, swaggerApiKey);
        swaggerDriver.executePost(request);


    }

    private Optional<String> generateApiSpec() throws IOException {
        ApiGatewayApiInfo apiGatewayApiInfo = new ApiGatewayApiInfo(stage, apiGateway, apiGatewayRestApiId);
        return apiGatewayApiInfo.generateOpenApiNoExtensions();

    }


}
