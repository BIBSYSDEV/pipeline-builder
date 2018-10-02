package no.bibsys.cloudformation;

public class CloudFormationConfigrable {

    public static final String MASTER_BRANCH="master";

    protected final String projectId;
    protected final String branchName;



    public CloudFormationConfigrable(String projectId, String branchName) {
        this.projectId = projectId;
        this.branchName = branchName;
    }


    public String devOrProd(){
        if(branchName.equalsIgnoreCase(MASTER_BRANCH)){
            return "prod";
        }
        else{
            return "dev";
        }
    }


    public String format(String... args){
        return String.join("-",args);

    }

    public final String getProjectId() {
        return projectId;
    }

    public final String getBranchName() {
        return branchName;
    }

}
