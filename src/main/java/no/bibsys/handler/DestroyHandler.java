package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import java.net.URISyntaxException;
import no.bibsys.handler.requests.buildevents.BuildEvent;
import no.bibsys.handler.templates.CodePipelineFunctionHandlerTemplate;

public class DestroyHandler extends CodePipelineFunctionHandlerTemplate<String> {


    @Override
    protected String processInput(BuildEvent input, Context context)
        throws IOException, URISyntaxException {

        String message = "Destroying stuff!!!";
        System.out.println(message);
        return message;


    }
}
