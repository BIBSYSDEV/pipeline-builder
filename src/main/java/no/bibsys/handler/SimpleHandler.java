package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleHandler extends HandlerHelper<String,String> {


    static final Logger logger = LogManager.getLogger(SimpleHandler.class);
    public SimpleHandler() {
        super(String.class, String.class);
    }

    protected String processInput(String request, Context context) throws IOException {

//        Map<String,Object> input=(Map<String,Object>) request;
       System.out.println(request);
       logger.info(request);

        return request;

    }




}


