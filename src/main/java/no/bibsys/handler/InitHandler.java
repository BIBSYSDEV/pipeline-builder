package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.apigateway.ApiExporter;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.handler.requests.PublishApi;
import no.bibsys.swaggerhub.SwaggerDriver;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

public class InitHandler extends  HandlerHelper<PublishApi,String> {

    public InitHandler() {
        super(PublishApi.class);
    }

    @Override
    public String processInput(final PublishApi input, Context context)
        throws IOException, URISyntaxException {

        CloudFormationConfigurable config=new CloudFormationConfigurable(input.getRepository(),input.getBranch());
        String json=generateApiSpec(input, config);
//
        SwaggerDriver swaggerDriver=new SwaggerDriver(input.getSwaggetHubApiKey(),
            input.getSwaggerOrganization(),input.getApiId());
        HttpPost request = swaggerDriver
            .updateSpecificationPostRequest(json, input.getApiVersion());
        swaggerDriver.executeUpdate(request);

        HttpGet getSpecRequest = swaggerDriver
            .getSpecificationVesionRequest(input.getApiVersion());
        String response=swaggerDriver.executeGet(getSpecRequest);
        return response;

    }

    private String generateApiSpec(PublishApi input, CloudFormationConfigurable config)
        throws IOException {
        ApiExporter apiExporter=new ApiExporter(config,input.getStage());
        return apiExporter.generateOpenApiNoExtensions();

    }
}
