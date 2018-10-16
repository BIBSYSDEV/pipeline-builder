package no.bibsys.utils;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ApiMessageParser<T> {

    private static final  Logger logger = LogManager.getLogger(ApiMessageParser.class);

    public T getBodyElementFromJson(String inputString, Class<T> tclass) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        Optional<JsonNode> tree = Optional
            .ofNullable(mapper.readTree(new StringReader(inputString)));
        JsonNode body = tree.map(node -> node.get("body")).orElse(null);

        if (tclass.equals(String.class)) {
            return (T) body.asText();
        } else {
            T request = null;
            if (body != null) {
                // body should always be a string for A lambda function connected to the API
                if (body.isValueNode()) {
                    request = parseBody(mapper, body.asText(), tclass);
                } else {
                    request = parseBody(mapper, body, tclass);
                }
            }

            return request;
        }


    }


    private T parseBody(ObjectMapper mapper, JsonNode node, Class<T> tclass) throws IOException {
        return mapper.readValue(new TreeTraversingParser(node), tclass);
    }


    private T parseBody(ObjectMapper mapper, String json, Class<T> tclass) {
        try {
            T object = mapper.readValue(json, tclass);
            return object;
        } catch (IOException e) {
            logger.error("Error parsing json string:{}", json);
            logger.error(e.getMessage());
            return null;
        }
    }
}
