package no.bibsys.git.github;

import java.io.IOException;
import no.bibsys.secrets.SecretsReader;

public class GithubConf implements GitInfo {

    public static String AWS_SECRET_NAME = "github";
    public static String AWS_SECRET_KEY = "read_from_github";
    private final transient String owner;
    private final transient String repo;
    private final transient String oauth;



    public GithubConf(String owner, String repo) throws IOException {
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
        SecretsReader secretsReader = new SecretsReader();
        return secretsReader.readAuthFromSecrets(AWS_SECRET_NAME, AWS_SECRET_KEY);

    }


}
