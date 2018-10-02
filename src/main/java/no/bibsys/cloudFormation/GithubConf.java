package no.bibsys.cloudFormation;

import no.bibsys.EnvUtils;

public class GithubConf implements EnvUtils {

    public final String auth;
    public final  String owner;
    public final String repo;


    public GithubConf(String  owner,String repo){
        this.owner=owner;
        this.repo=repo;
        this.auth=getEnvVariable("oauth");
    }

}
