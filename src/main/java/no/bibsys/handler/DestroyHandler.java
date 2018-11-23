package no.bibsys.handler;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.http.client.methods.HttpDelete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.lambda.runtime.Context;
import no.bibsys.handler.requests.buildevents.BuildEvent;
import no.bibsys.handler.responses.SimpleResponse;
import no.bibsys.handler.templates.SwaggerHubEditor;
import no.bibsys.swaggerhub.SwaggerDriver;

public class DestroyHandler extends SwaggerHubEditor {

    private final static Logger logger = LoggerFactory.getLogger(DestroyHandler.class);

    @Override
    protected SimpleResponse processInput(BuildEvent input, String apiGatewayInputString,
        Context context)
        throws IOException, URISyntaxException {
        initFields();

        SwaggerDriver swaggerDriver=new SwaggerDriver(swaggerHubApiKey,swaggerOrganization,apiId);
        HttpDelete deleteRequest = swaggerDriver
            .createDeleteApiRequest();
        Integer response = swaggerDriver.executeDelete(deleteRequest);

        logger.info(String.valueOf(response));
        return new SimpleResponse(response.toString());

    }
}
