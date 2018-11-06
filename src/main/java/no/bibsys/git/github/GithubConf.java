package no.bibsys.git.github;

import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.utils.Environment;

public class GithubConf implements  GitInfo{

    private final transient String owner;
    private final transient String repo;
    private final transient String oauth;

    private final transient Environment env;


    public GithubConf(String owner, String repo, Environment env) throws IOException {

        this.env = env;
        this.owner = initOwner(owner);
        this.repo = initRepo(repo);
        this.oauth = initOAuth();
    }


    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getRepo() {
        return repo;
    }

    @Override
    public String getOauth() {
        return oauth;
    }


    private String initRepo(String repo) {
        return repo;
    }

    private String initOwner(String owner) {
        return owner;
    }

    private String initOAuth() throws IOException {
        Optional<String> envAuth = env.readEnvOpt("GITHUBAUTH");
        if (envAuth.isPresent()) {
            return envAuth.get();
        } else {
            return readAuthFromSecrets();
        }
    }


    private String readAuthFromSecrets() throws IOException {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
            .withRegion(Region.EU_Ireland.toString())
            .build();
        ObjectMapper mapper = new ObjectMapper();

        Optional<GetSecretValueResult> getSecretValueResult = readAuthKey(client);

        if (getSecretValueResult.map(result -> result.getSecretString()).isPresent()) {
            String secret = getSecretValueResult.get().getSecretString();
            String value = mapper.readTree(secret)
                .findValuesAsText("githubapikey").stream().findFirst().orElse(null);
            return value;
        }
        return null;
    }

    private Optional<GetSecretValueResult> readAuthKey(AWSSecretsManager client) {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
            .withSecretId("githubapikey");
        Optional<GetSecretValueResult> getSecretValueResult = Optional.empty();
        try {
            getSecretValueResult = Optional.ofNullable(client
                .getSecretValue(getSecretValueRequest));
        } catch (InvalidRequestException e) {
            getSecretValueResult = Optional.empty();
        }
        return getSecretValueResult;
    }


}
