package no.bibsys.handler.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.utils.JsonUtils;

public class PushEvent {



    public PushEvent(String jsonString) throws IOException {

        ObjectMapper mapper= JsonUtils.newJsonParser();
        JsonNode root=mapper.readValue(jsonString,JsonNode.class);
        String ref=root.get("ref")

    }



}
