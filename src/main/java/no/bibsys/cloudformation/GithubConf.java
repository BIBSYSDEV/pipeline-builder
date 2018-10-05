package no.bibsys.cloudformation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import no.bibsys.utils.EnvUtils;

public class GithubConf implements EnvUtils {

    private final String auth;
    private final  String owner;
    private final String repo;

    private Config config= ConfigFactory.load();

    public GithubConf(){
        config.resolve();
        this.owner=config.getString("git.owner");
        this.repo=config.getString("git.repo");
        this.auth=config.getString("git.oauth");
    }


    public String getAuth() {
        return auth;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }





}
