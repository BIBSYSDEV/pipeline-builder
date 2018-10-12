package no.bibsys.utils;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiMessageParser<T> {

    Logger logger=LoggerFactory.getLogger(ApiMessageParser.class);

    public T getBodyElementFromJson(String inputString, Class<T> tClass) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        Optional<JsonNode> tree = Optional.ofNullable(mapper.readTree(new StringReader(inputString)));
        JsonNode body = tree.map(node -> node.get("body")).orElse(null);

        if(tClass.equals(String.class)){
            return (T)body.asText();
        }
        else{
            T request=null;
            if(body!=null){
                // body should always be a string for A lambda fuction connected to the API
                if(body.isValueNode()){
                    request=parseBody(mapper, body.asText(),tClass);
                }
                else{
                    request=parseBody(mapper,body,tClass);
                }
            }

            return request;
        }


    }



    private T parseBody(ObjectMapper mapper,JsonNode node, Class<T> tclass) throws IOException {
        try{

            T result = mapper.readValue(new TreeTraversingParser(node), tclass);
            return result;
        }
        catch(Exception e){
            String json="null";
            if(node!=null)
                json=node.toString();
            logger.error("Error parsing JsonNode:{}",json);
            return null;
        }
    }


    private T parseBody(ObjectMapper mapper, String json, Class<T> tclass) {
        try {
            T object = mapper.readValue(json, tclass);
            return object;
        } catch (IOException e) {
            logger.error("Error parsing json string:{}",json);
            logger.error(e.getMessage());
            return null;
        }
    }
}
