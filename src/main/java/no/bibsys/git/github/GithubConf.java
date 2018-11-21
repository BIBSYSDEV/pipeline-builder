package no.bibsys.git.github;

import java.io.IOException;
import no.bibsys.secrets.SecretsReader;

public class GithubConf implements GitInfo {

    public static String AWS_SECRET_NAME = "github";
    public static String AWS_SECRET_KEY = "read_from_github";
    private final transient String owner;
    private final transient String repo;




    public GithubConf(String owner, String repo)  {
        this.owner = initOwner(owner);
        this.repo = initRepo(repo);

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
    public String getOauth() throws IOException {
        SecretsReader secretsReader = new SecretsReader(AWS_SECRET_NAME, AWS_SECRET_KEY);
        return secretsReader.readSecret();
    }


    private String initRepo(String repo) {
        return repo;
    }

    private String initOwner(String owner) {
        return owner;
    }




}
