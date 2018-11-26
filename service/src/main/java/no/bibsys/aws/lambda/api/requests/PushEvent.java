package no.bibsys.aws.lambda.api.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.git.github.GitInfoImpl;
import no.bibsys.aws.tools.JsonUtils;

public final class PushEvent extends GitInfoImpl {


    public PushEvent() {
        super();
    }

    private PushEvent(JsonNode root) {
        super();
        this.setBranch(initBranch(root));
        this.setOwner(initOwner(root));
        this.setRepository(initRepository(root));

    }


    public static Optional<GitInfo> create(String json) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readValue(json, JsonNode.class);
        if (root.has("pusher")) {
            return Optional.of(new PushEvent(root));
        } else {
            return Optional.empty();
        }

    }


    private String initBranch(JsonNode root) {
        Path ref = Paths.get(root.get("ref").asText());
        String branch = ref.getFileName().toString();
        return branch;
    }


    private String initOwner(JsonNode root) {
        JsonNode repository = getRepositoryDetails(root);
        return repository.get("owner").get("name").asText();
    }


    private String initRepository(JsonNode root) {
        JsonNode repository = getRepositoryDetails(root);
        return repository.get("name").asText();
    }

    private JsonNode getRepositoryDetails(JsonNode root) {
        return root.get("repository");
    }


    @Override
    public String toString() {
        return String.join(":", getRepository(), getBranch());
    }
}
