package no.bibsys.handler;


import java.io.IOException;

public class SimpleHandler extends HandlerHelper<String,String> {


    public SimpleHandler() {
        super(String.class, String.class);
    }

    protected String processInput(String request) throws IOException {
       return request;

    }




}


