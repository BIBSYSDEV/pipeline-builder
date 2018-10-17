package no.bibsys.handler.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class PullRequest {

    public static final String ACTION_OPEN = "opened";
    public static final String ACTION_REOPEN = "reopened";
    public static final String ACTION_CLOSE = "closed";


    private final transient JsonNode root;
    private final transient String action;
    private final transient String branch;
    private final transient String repositoryName;
    private final transient String owner;


    public PullRequest(String jsonString) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        root = mapper.readTree(jsonString);
        this.action = initAction();
        this.branch = initBranch();
        this.owner = initOwner();
        repositoryName = initRepositoryName();
    }

    private String initOwner() {
        return root.get("repository").get("owner").get("login").asText();
    }

    private String initRepositoryName() {
        return root.get("repository").get("name").asText();
    }


    private String initAction() {
        return root.get("action").asText();
    }


    private String initBranch() {
        String branch = root.get("pull_request").get("head").get("ref").asText();
        return branch;
    }

    public String getBranch() {
        return branch;
    }

    public String getAction() {
        return action;
    }


    public String getRepositoryName() {
        return this.repositoryName;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.join(":", getRepositoryName(), getBranch(), getAction());
    }

}
