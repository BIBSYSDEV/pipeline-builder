package no.bibsys.handler;


import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHandler extends HandlerHelper<String,String> {


    Logger logger=LoggerFactory.getLogger(SimpleHandler.class);
    public SimpleHandler() {
        super(String.class, String.class);
    }

    protected String processInput(String request) throws IOException {

//        Map<String,Object> input=(Map<String,Object>) request;
        logger.info(request);



        return request;

    }




}


