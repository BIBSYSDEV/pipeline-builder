package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.handler.requests.CustomBuildRequest;
import no.bibsys.handler.requests.PullRequest;
import no.bibsys.utils.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomBranchBuilder extends HandlerHelper<CustomBuildRequest, String> {


    private static final Logger logger = LogManager.getLogger(CustomBranchBuilder.class);


    public CustomBranchBuilder() {
        super(CustomBuildRequest.class);
    }

    @Override
    protected String processInput(CustomBuildRequest request, Context context) throws IOException {

        Environment env = new Environment();

        if (request.getAction().equals(CustomBuildRequest.CREATE)) {
            createStacks(request.getOwner(),request.getRepositoryName(),request.getBranch(), env);
        }

        if (request.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(request.getOwner(),request.getRepositoryName(),request.getBranch(), env);
        }

        System.out.println(request.toString());
        logger.info(request.toString());
        ObjectMapper objectMapper=new ObjectMapper();
        String requestJson=objectMapper.writeValueAsString(request);


        return requestJson;

    }





}


