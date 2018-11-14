package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.handler.requests.buildevents.BuildEvent;
import no.bibsys.handler.responses.SimpleResponse;
import no.bibsys.handler.templates.SwaggerHubEditor;
import no.bibsys.swaggerhub.SwaggerDriver;
import org.apache.http.client.methods.HttpDelete;

public class DestroyHandler extends SwaggerHubEditor {


    @Override
    protected SimpleResponse processInput(BuildEvent input, String apiGatewayInputString,
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
