package no.bibsys.lambda.api.handlers;


import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import no.bibsys.lambda.api.utils.Action;
import no.bibsys.lambda.api.requests.UpdateStackRequest;
import no.bibsys.utils.JsonUtils;

public class UpdateStackRequestHandler extends GithubHandler {


    @Override
    public String processInput(String string, Map<String, String> headers, Context context)
        throws IOException {

        ObjectMapper mapper = JsonUtils.newJsonParser();
        UpdateStackRequest request = mapper.readValue(string, UpdateStackRequest.class);

        if (request.getAction().equals(Action.CREATE)) {
            createStacks(request);
        }

        if (request.getAction().equals(Action.DELETE)) {
            deleteStacks(request);
        }

        System.out.println(request.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        return requestJson;

    }



}


