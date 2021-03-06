package no.bibsys.aws.lambda.api.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.aws.tools.JsonUtils;

public final class SimplePullRequest extends GitEvent {

    public static final String ACTION_OPEN = "opened";
    public static final String ACTION_REOPEN = "reopened";
    public static final String ACTION_CLOSE = "closed";
    private static final String REPOSITORY = "repository";
    private static final String OWNER = "owner";
    private static final String LOGIN = "login";
    private static final String NAME = "name";
    private static final String PULL_REQUEST = "pull_request";
    private static final String HEAD = "head";
    private static final String REF = "ref";
    private static final String ACTION = "action";

    private String action;

    public SimplePullRequest() {
        super();
    }

    private SimplePullRequest(JsonNode root) {
        super();
        this.setOwner(root.get(REPOSITORY).get(OWNER).get(LOGIN).asText());
        this.setRepository(root.get(REPOSITORY).get(NAME).asText());
        this.setBranch(root.get(PULL_REQUEST).get(HEAD).get(REF).asText());
        this.action = root.get(ACTION).asText();
    }

    public static Optional<GitEvent> create(String jsonString) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(jsonString);

        if (root.has(PULL_REQUEST)) {
            return Optional.of(new SimplePullRequest(root));
        } else {
            return Optional.empty();
        }
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return action;
    }
}
