package no.bibsys.cloudformation;


import org.apache.commons.codec.digest.DigestUtils;

public class CloudFormationConfigurable {

    public static final String MASTER_BRANCH="master";

    protected final String projectId;
    protected final String branchName;



    protected final String randomId;
    protected final String stage;



    public CloudFormationConfigurable(String projectId, String branchName) {
        this.projectId = projectId;
        this.branchName = branchName;
        this.stage=testOrProd();
        this.randomId=DigestUtils.sha1Hex(branchName);
    }


    public String testOrProd(){
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

    public String getStage() {
        return stage;
    }

    public String getRandomId() {
        return randomId;
    }
}
