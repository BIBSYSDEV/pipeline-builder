package no.bibsys.handler;


import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.bibsys.handler.requests.Action;
import no.bibsys.handler.requests.CustomBuildRequest;
import no.bibsys.utils.JsonUtils;

public class UpdateStackRequestHandler extends GithubHandler {

    private final static Logger logger = LoggerFactory.getLogger(UpdateStackRequestHandler.class);

    @Override
    public String processInput(String string, Map<String, String> headers, Context context)
        throws IOException {

        ObjectMapper mapper = JsonUtils.newJsonParser();
        CustomBuildRequest request = mapper.readValue(string, CustomBuildRequest.class);

        if (request.getAction().equals(Action.CREATE)) {
            createStacks(request);
        }

        if (request.getAction().equals(Action.DELETE)) {
            deleteStacks(request);
        }

        logger.info(request.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        return requestJson;

    }



}


