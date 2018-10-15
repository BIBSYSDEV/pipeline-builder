package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import no.bibsys.utils.PullRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleHandler extends HandlerHelper<String, String> {


    static final Logger logger = LogManager.getLogger(SimpleHandler.class);

    public SimpleHandler() {
        super(String.class, String.class);
    }

    protected String processInput(String request, Context context) throws IOException {
        PullRequest pullRequest = new PullRequest(request);

        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return request;

    }


}


