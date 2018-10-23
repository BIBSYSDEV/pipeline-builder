package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.git.github.RestReader;
import no.bibsys.handler.requests.CustomBuildRequest;
import no.bibsys.utils.Environment;
import no.bibsys.utils.JsonUtils;

public class CustomBranchBuilder extends SimpleHandler {


    @Override
    protected String processInput(String string, Context context) throws IOException {

        ObjectMapper mapper = JsonUtils.newJsonParser();
        CustomBuildRequest request = mapper.readValue(string, CustomBuildRequest.class);

        if (request.getAction().equals(CustomBuildRequest.CREATE)) {
            createStacks(initGithubReader(request));
        }

        if (request.getAction().equals(CustomBuildRequest.DELETE)) {
            deleteStacks(initGithubReader(request));
        }

        System.out.println(request.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
        return requestJson;

    }


    private GithubReader initGithubReader(CustomBuildRequest request) throws IOException {
        GithubConf githubConf = new GithubConf(request.getOwner(), request.getRepositoryName(),
            new Environment());
        return new GithubReader(new RestReader(githubConf), request.getBranch());
    }


}


