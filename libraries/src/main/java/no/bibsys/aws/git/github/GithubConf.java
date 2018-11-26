package no.bibsys.aws.git.github;

import java.io.IOException;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

public class GithubConf implements GitInfo {

    public static final String REPO_OWNER = "OWNER";
    public static final String REPOSITORY = "REPOSITORY";
    public static final String BRANCH = "BRANCH";

    public static String AWS_SECRET_NAME = "github";
    public static String AWS_SECRET_KEY = "read_from_github";
    private final transient String owner;
    private final transient String repo;
    private final transient String branch;


    public GithubConf(Environment environment) {
        this.owner = environment.readEnv(REPO_OWNER);
        this.repo = environment.readEnv(REPOSITORY);
        this.branch = environment.readEnv(BRANCH);

    }


    public GithubConf(String owner, String repo, String branch) {
        this.owner = initOwner(owner);
        this.repo = initRepo(repo);
        this.branch = branch;

    }


    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getRepository() {
        return repo;
    }

    @Override
    public String getOauth() throws IOException {
        SecretsReader secretsReader = new SecretsReader(AWS_SECRET_NAME, AWS_SECRET_KEY);
        return secretsReader.readSecret();
    }


    @Override
    public String getBranch() {
        return branch;
    }


    private String initRepo(String repo) {
        return repo;
    }

    private String initOwner(String owner) {
        return owner;
    }




}
