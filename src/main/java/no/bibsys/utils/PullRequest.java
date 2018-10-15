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
    private final String repositoryName;
    private final String owner;


    public PullRequest(String json) throws IOException {
        this.json = json;
        mapper = new ObjectMapper();
        root = mapper.readTree(json);
        this.action=initAction();
        this.branch=initBranch();
        this.owner=initOwner();
        repositoryName = initRepositoryName();
    }

    private String initOwner() {
        return root.get("repository").get("owner").get("login").asText();
    }

    private String initRepositoryName() {
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


    public String getRepositoryName(){
        return this.repositoryName;
    }

    public String getOwner() {
        return owner;
    }

    public String toString(){
        return String.join(":", getRepositoryName(),getBranch(),getAction());
    }

}
