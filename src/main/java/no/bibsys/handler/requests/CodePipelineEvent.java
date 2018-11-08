package no.bibsys.handler.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.utils.JsonUtils;


public class CodePipelineEvent {

    private final String id;

    public CodePipelineEvent(String id) {
        this.id = id;
    }


    public static CodePipelineEvent create(String eventJsonString) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(eventJsonString);
        String id= root.get("CodePipeline.job").get("id").asText();
        return new CodePipelineEvent(id);
    }


    public String getId() {
        return id;
    }


}


