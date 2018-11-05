package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.handler.requests.Action;
import no.bibsys.handler.requests.CustomBuildRequest;
import no.bibsys.utils.JsonUtils;

public class CustomBranchBuilder extends SimpleHandler {


    @Override
    public String processInput(String string, Context context) throws IOException {

        ObjectMapper mapper = JsonUtils.newJsonParser();
        CustomBuildRequest request = mapper.readValue(string, CustomBuildRequest.class);

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


