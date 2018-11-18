package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.lambda.deploy.requests.DeployEvent;
import no.bibsys.lambda.responses.SimpleResponse;
import no.bibsys.swaggerhub.SwaggerDriver;
import org.apache.http.client.methods.HttpDelete;

public class DestroyHandler extends SwaggerHubUpdater {


    public DestroyHandler() throws IOException {
        super();
    }

    @Override
    protected SimpleResponse processInput(DeployEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {
        initFields();

        SwaggerDriver swaggerDriver=new SwaggerDriver(swaggerHubApiKey,swaggerOrganization,apiId);
        HttpDelete deleteRequest = swaggerDriver
            .createDeleteApiRequest();
        Integer response = swaggerDriver.executeDelete(deleteRequest);

        System.out.println(response);
        return new SimpleResponse(response.toString());

    }
}
