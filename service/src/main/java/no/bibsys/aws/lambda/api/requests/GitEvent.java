package no.bibsys.aws.lambda.api.requests;

public class GitEvent {

    private String gitOwner;
    private String gitRepository;
    private String gitBranch;


    public GitEvent() {
    }

    public GitEvent(String gitOwner, String gitRepository, String gitBranch) {
        this.gitOwner = gitOwner;
        this.gitRepository = gitRepository;
        this.gitBranch = gitBranch;
    }


    public String getGitOwner() {
        return gitOwner;
    }

    public void setGitOwner(String gitOwner) {
        this.gitOwner = gitOwner;
    }

    public String getGitRepository() {
        return gitRepository;
    }

    public void setGitRepository(String gitRepository) {
        this.gitRepository = gitRepository;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

}
