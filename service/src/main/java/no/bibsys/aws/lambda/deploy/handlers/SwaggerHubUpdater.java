package no.bibsys.aws.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.lambda.deploy.handlers.utils.ApiGatewayApiInfo;
import no.bibsys.aws.secrets.SecretsReader;
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
 *
 * If the branch is master then the API name in SwaggerHub is the one specified in the constructor.
 * If the branch is not the master then the API name in SwaggerHub is the name of the Stack.
 */

public class SwaggerHubUpdater {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerHubUpdater.class);
    private final transient SwaggerHubInfo swaggerHubInfo;
    private final transient AmazonApiGateway apiGateway;
    private final transient String apiGatewayRestApiId;
    protected transient Stage stage;

    public SwaggerHubUpdater(
        AmazonApiGateway apiGateway,
        String apiGatewayRestApiId,
        SwaggerHubInfo swaggerHubInfo,
        SecretsReader swaggerHubSecretsReader,
        Stage stage,
        String stackName,
        GitInfo gitInfo)  {
        this.apiGateway = apiGateway;
        this.apiGatewayRestApiId = apiGatewayRestApiId;
        this.stage = stage;
        this.swaggerHubInfo = intializeSwaggerHubInfo(swaggerHubInfo, gitInfo, stackName,
            swaggerHubSecretsReader);
    }

    private SwaggerHubInfo intializeSwaggerHubInfo(SwaggerHubInfo swaggerHubInfo, GitInfo gitInfo,
        String stackName,SecretsReader secretsReader) {
        String branch = gitInfo.getBranch();
        if (branch.equalsIgnoreCase(GitConstants.MASTER)) {
            return swaggerHubInfo;
        } else {
            String org = swaggerHubInfo.getSwaggerOrganization();
            String version = swaggerHubInfo.getApiVersion();
            //If it is not the master branch then do not overwrite the production API.
            // Instead, create an API using the stack name.
            return new SwaggerHubInfo(stackName, version, org,secretsReader);
        }
    }

    public int deleteApiVersion() throws URISyntaxException, IOException {
        String swaggerApiKey = swaggerHubInfo.getSwaggerAuth();
        SwaggerDriver swaggerDriver = new SwaggerDriver(swaggerHubInfo);
        HttpDelete deleteRequest = swaggerDriver.createDeleteVersionRequest(swaggerApiKey);
        return swaggerDriver.executeDelete(deleteRequest);
    }

    /**
     * Deletes the whole API documentation from SwaggerHub.
     *
     * @return Success of Failure code the delete request
     */
    public int deleteApi() throws URISyntaxException, IOException {
        String swaggerApiKey = swaggerHubInfo.getSwaggerAuth();
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

    private String readTheUpdatedAPI(SwaggerDriver swaggerDriver)
        throws URISyntaxException, IOException {
        String swaggerApiKey = swaggerHubInfo.getSwaggerAuth();
        HttpGet getSpecRequest = swaggerDriver.getSpecificationRequest(swaggerApiKey);
        return swaggerDriver.executeGet(getSpecRequest);
    }

    private SwaggerDriver newSwaggerDriver() {
        return new SwaggerDriver(swaggerHubInfo);
    }

    private void executeUpdate(String json, SwaggerDriver swaggerDriver)
        throws URISyntaxException, IOException {
        String swaggerApiKey = swaggerHubInfo.getSwaggerAuth();
        HttpPost request = swaggerDriver.createUpdateRequest(json, swaggerApiKey);
        swaggerDriver.executePost(request);
    }

    private Optional<String> generateApiSpec() throws IOException {
        ApiGatewayApiInfo apiGatewayApiInfo = new ApiGatewayApiInfo(stage, apiGateway,
            apiGatewayRestApiId);
        return apiGatewayApiInfo.generateOpenApiNoExtensions();
    }

    public SwaggerHubInfo getSwaggerHubInfo() {
        return swaggerHubInfo;
    }
}
