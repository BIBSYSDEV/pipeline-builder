package no.bibsys.handler.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import no.bibsys.utils.JsonUtils;

public final class PushEvent implements GitEvent {


    private final transient JsonNode root;



    private PushEvent(JsonNode root)  {
        this.root = root;

    }


    public static Optional<GitEvent> create(String json) throws IOException {
        ObjectMapper mapper=JsonUtils.newJsonParser();
        JsonNode root=mapper.readValue(json,JsonNode.class);
        if(root.has("pusher")){
            return Optional.of(new PushEvent(root));
        }
        else {
            return Optional.empty();
        }

    }


    @Override
    public String getBranch() {
        Path ref = Paths.get(root.get("ref").asText());
        String branch = ref.getFileName().toString();
        return branch;
    }

    @Override
    public String getOwner() {
        JsonNode repository = getRepositoryDetails();
        return repository.get("owner").get("name").asText();
    }

    @Override
    public String getRepository() {
        JsonNode repository = getRepositoryDetails();
        return repository.get("name").asText();
    }

    private JsonNode getRepositoryDetails() {
        return root.get("repository");
    }


    @Override
    public String toString() {
        return String.join(":", getRepository(), getBranch());
    }
}
