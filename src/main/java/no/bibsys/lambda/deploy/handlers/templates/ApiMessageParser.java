package no.bibsys.lambda.deploy.handlers.templates;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import no.bibsys.utils.JsonUtils;


public class ApiMessageParser<T> {

private final transient ObjectMapper mapper = JsonUtils.newJsonParser();

    public Map<String, String> getHeadersFromJson(String inputString) throws IOException {
        JsonNode root = mapper.readTree(new StringReader(inputString));
        JsonNode headers = root.get("headers");
        Map<String, String> headersMap = (Map<String, String>) mapper
            .convertValue(headers, Map.class);
        return headersMap;

    }


    public T getBodyElementFromJson(String inputString, Class<T> tclass) throws IOException {

        Optional<JsonNode> tree = Optional
            .ofNullable(mapper.readTree(new StringReader(inputString)));
        JsonNode body = tree.map(node -> node.get("body")).orElse(null);

        if (tclass.equals(String.class)) {
            return (T) body.asText();
        } else {
            T request = null;
            if (body != null) {
                // body should always be a string for a lambda function connected to the API
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


    private T parseBody(ObjectMapper mapper, String json, Class<T> tclass) throws IOException {

        T object = mapper.readValue(json, tclass);
        return object;

    }
}
