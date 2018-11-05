package no.bibsys.handler.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.utils.JsonUtils;

public final class PullRequest extends RepositoryInfo {

    public static final String ACTION_OPEN = "opened";
    public static final String ACTION_REOPEN = "reopened";
    public static final String ACTION_CLOSE = "closed";


    private  String action;


    public PullRequest(){
        super();
    }

    private PullRequest(JsonNode root) throws IOException {
        super();
        this.setOwner(root.get("repository").get("owner").get("login").asText());
        this.setRepository(root.get("repository").get("name").asText());
        this.setBranch(root.get("pull_request").get("head").get("ref").asText());
        this.action=root.get("action").asText();

    }

    public static Optional<RepositoryInfo> create(String jsonString) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(jsonString);

        if (root.has("pull_request")) {
            return Optional.of(new PullRequest(root));
        } else {
            return Optional.empty();
        }
    }





    public String getAction() {
        return action;
    }

    public void setAction(String action){
        this.action=action;
    }







}
