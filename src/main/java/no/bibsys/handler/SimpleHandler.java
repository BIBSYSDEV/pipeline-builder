package no.bibsys.handler;


import no.bibsys.handler.requests.BranchRequest;

public class SimpleHandler extends no.bibys.handlers.HandlerHelper<BranchRequest,String> {


    public SimpleHandler() {
        super(BranchRequest.class, String.class);
    }

    protected String processInput(BranchRequest request){
        String branch=request.getBranch();
        return branch;
    }




}


