package no.bibsys.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public final class JsonUtils {


    private JsonUtils() {
        throw new UnsupportedOperationException();
    }

    public static ObjectMapper newJsonParser() {
        JsonFactory jsonFactory = new JsonFactory()
            .configure(Feature.ALLOW_COMMENTS, true)
            .configure(Feature.ALLOW_YAML_COMMENTS, true);
        return new ObjectMapper(jsonFactory);
    }


    public static String removeComments(String jsonWithComments) throws IOException {
        ObjectMapper mapper = newJsonParser();
        JsonNode jsonNode = mapper.readTree(jsonWithComments);
        String validJson = mapper.writeValueAsString(jsonNode);

        return validJson;

    }


}
