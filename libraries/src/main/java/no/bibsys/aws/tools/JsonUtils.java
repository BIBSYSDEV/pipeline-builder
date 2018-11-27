package no.bibsys.aws.tools;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;

public final class JsonUtils {


    private JsonUtils() {
        throw new UnsupportedOperationException();
    }

    public static ObjectMapper newJsonParser() {
        JsonFactory jsonFactory =
                new JsonFactory().configure(Feature.ALLOW_COMMENTS, true).configure(Feature.ALLOW_YAML_COMMENTS, true);
        return new ObjectMapper(jsonFactory);
    }



    public static ObjectMapper newYamlParser() {
        YAMLFactory factory = new YAMLFactory();
        return new ObjectMapper(factory);
    }



    public static String yamlToJson(String yaml) throws IOException {
        ObjectMapper yamlParser = newYamlParser();
        JsonNode root = yamlParser.readTree(yaml);
        ObjectMapper jsonParser = newJsonParser();
        return jsonParser.writeValueAsString(root);

    }

    public static String removeComments(String jsonWithComments) throws IOException {
        ObjectMapper mapper = newJsonParser();
        JsonNode jsonNode = mapper.readTree(jsonWithComments);
        String validJson = mapper.writeValueAsString(jsonNode);

        return validJson;

    }


}
