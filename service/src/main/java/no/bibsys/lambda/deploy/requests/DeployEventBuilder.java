package no.bibsys.lambda.deploy.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.aws.tools.JsonUtils;

public final class DeployEventBuilder {


    private DeployEventBuilder() {
        throw new IllegalStateException(this.getClass().getName()+ " should not be initialized");
    }


    public static DeployEvent create(String eventJsonString) throws IOException {
        if ( eventJsonString != null && !eventJsonString.isEmpty()) {
            return readEventFromString(eventJsonString);
        }
        else{
            return new GenericEvent();
        }


    }

    private static DeployEvent readEventFromString(String eventJsonString) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(eventJsonString);
        if (isCodePipelineEvent(root)) {
            String id = root.get("CodePipeline.job").get("id").asText();
            return new CodePipelineEvent(id);
        }
        else{
            return new GenericEvent();
        }
    }

    private static boolean isCodePipelineEvent(JsonNode root) {
        return root.has("CodePipeline.job") &&
            root.get("CodePipeline.job").has("id");
    }


}
