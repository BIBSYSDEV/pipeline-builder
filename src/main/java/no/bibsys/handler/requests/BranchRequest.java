package no.bibsys.handler.requests;

public class BranchRequest {


      String projectId;
      String branch;



    public BranchRequest(){};


    public BranchRequest(String projectId, String branch) {
        this.projectId = projectId;
        this.branch = branch;
    }



    public String getProjectId() {
        return projectId;
    }

    public String getBranch() {
        return branch;
    }


    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
