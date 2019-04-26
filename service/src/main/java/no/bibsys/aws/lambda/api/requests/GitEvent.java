package no.bibsys.aws.lambda.api.requests;

public class GitEvent {

    private String owner;
    private String repository;
    private String branch;


    public GitEvent() {
    }

    public GitEvent(String owner, String gitRepository, String gitBranch) {
        this.owner = owner;
        this.repository = gitRepository;
        this.branch = gitBranch;
    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String gitRepository) {
        this.repository = gitRepository;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String gitBranch) {
        this.branch = gitBranch;
    }

}
