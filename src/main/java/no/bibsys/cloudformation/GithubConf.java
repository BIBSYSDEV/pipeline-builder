package no.bibsys.cloudformation;

import no.bibsys.utils.EnvUtils;

public class GithubConf extends EnvUtils {
    private final  String owner;
    private final String repo;

    private final String oauth;



    public GithubConf(String owner,String repo){
        this.oauth=initOAuth();
        this.owner=initOwner(owner);
        this.repo=initRepo(repo);
    }

    private String initRepo(String repo) {
       return repo;
    }

    private String initOwner(String owner){
        return  owner;
    }

    private String initOAuth() {
        return readEnv("GITHUBAUTH");
    }


    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public String getOauth() {
        return oauth;
    }



}
