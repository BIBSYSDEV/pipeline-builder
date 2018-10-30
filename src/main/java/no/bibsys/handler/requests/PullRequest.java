package no.bibsys.handler.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.utils.JsonUtils;

public final class PullRequest implements  GitEvent{

    public static final String ACTION_OPEN = "opened";
    public static final String ACTION_REOPEN = "reopened";
    public static final String ACTION_CLOSE = "closed";


    private final transient JsonNode root;


    private PullRequest(JsonNode root) throws IOException {
        this.root = root;
    }

    public static Optional<GitEvent> create(String jsonString) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(jsonString);
        if (root.has("pull_request")) {
            return Optional.of(new PullRequest(root));
        } else {
            return Optional.empty();
        }
    }


    @Override
    public String getOwner() {
        return root.get("repository").get("owner").get("login").asText();
    }

    @Override
    public String getRepository() {
        return root.get("repository").get("name").asText();
    }


    public String getAction() {
        return root.get("action").asText();
    }


    @Override
    public String getBranch() {
        String branch = root.get("pull_request").get("head").get("ref").asText();
        return branch;
    }


    @Override
    public String toString() {
        return String.join(":", getRepository(), getBranch(), getAction());
    }

}
