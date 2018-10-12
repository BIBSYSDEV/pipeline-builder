package no.bibsys.handler;


import java.io.IOException;
import java.util.Map;

public class SimpleHandler extends HandlerHelper<Map,String> {


    public SimpleHandler() {
        super(Map.class, String.class);
    }

    protected String processInput(Map request) throws IOException {
        Map<String,Object> input=(Map<String,Object>) request;
       return input.keySet().toString();

    }




}


