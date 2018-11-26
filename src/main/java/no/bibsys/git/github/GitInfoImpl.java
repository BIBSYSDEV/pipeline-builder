package no.bibsys.git.github;

public class GitInfoImpl implements GitInfo {

    protected transient String owner;
    protected transient String repository;
    protected transient String branch;

    public GitInfoImpl() {

    }


    public GitInfoImpl(String owner, String repository, String branch) {
        this.owner = owner;
        this.repository = repository;
        this.branch = branch;
    }


    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public String getOauth() {
        return null;
    }

    @Override
    public String getBranch() {
        return branch;
    }


    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }


}
