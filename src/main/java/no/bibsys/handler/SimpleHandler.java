package no.bibsys.handler;


import java.io.IOException;
import no.bibsys.handler.requests.BranchRequest;

public class SimpleHandler extends HandlerHelper<BranchRequest,String> {


    public SimpleHandler() {
        super(BranchRequest.class, String.class);
    }

    protected String processInput(BranchRequest request) throws IOException {
//        Application application=new Application();
//        application.run();
        return "Hello world";

    }




}


