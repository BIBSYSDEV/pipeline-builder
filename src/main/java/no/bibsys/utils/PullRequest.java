package no.bibsys.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class PullRequest {

    public static final String ACTION_OPEN = "opened";
    public static final String ACTION_CLOSE = "closed";

    private ObjectMapper mapper;
    private final String json;
    private final JsonNode root;


    private final String action;



    private final String branch;
    private final String projectId;


    public PullRequest(String json) throws IOException {
        this.json = json;
        mapper = new ObjectMapper();
        root = mapper.readTree(json);
        this.action=initAction();
        this.branch=initBranch();

        projectId = initProjectId();
    }

    private String initProjectId() {
        return root.get("repository").get("name").asText();
    }


    private String initAction(){
        return root.get("action").asText();
    }


    private String initBranch(){
        String branch=root.get("pull_request").get("head").get("ref").asText();
        return branch;
    }
    public String getBranch() {
        return branch;
    }
    public String getAction() {
        return action;
    }


    public String getProjectId(){
        return this.projectId;
    }


    public String toString(){
        return String.join(":",getProjectId(),getBranch(),getAction());
    }

}
