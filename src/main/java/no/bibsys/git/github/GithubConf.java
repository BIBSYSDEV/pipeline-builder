package no.bibsys.git.github;

import java.io.IOException;
import java.util.Optional;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;

public class GithubConf implements  GitInfo{

    private final transient String owner;
    private final transient String repo;
    private final transient String oauth;

    private final transient Environment env;


    public static String AWS_SECRET_NAME = "github";
    public static String AWS_SECRET_KEY = "read_from_github";


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
            SecretsReader secretsReader= new SecretsReader() ;
            return secretsReader.readAuthFromSecrets(AWS_SECRET_NAME, AWS_SECRET_KEY);
        }
    }







}
