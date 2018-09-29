package no.bibsys;

public class GithubCredentials implements EnvUtils{


  private final String owner;
  private final String repository;
  private final String branch;
  private final String oauth;


  public GithubCredentials(String owner, String repository,String branch ) {
    this.owner = owner;
    this.repository = repository;
    this.branch=branch;
    this.oauth = getEnvVariable("oauth");
  }


  public String getOwner(){
    return this.owner;
  }

  public String getRepository(){
    return  this.repository;
  }

  public String getOauth(){
    return this.oauth;
  }

  public String getBranch() {
    return branch;
  }



}
