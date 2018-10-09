package no.bibsys.cloudformation;


import org.apache.commons.codec.digest.DigestUtils;

public class CloudFormationConfigurable {

    public static final String MASTER_BRANCH = "master";
    private static final int maxBranchNameLength = 12;

    protected final String projectId;
    protected final String branchName;
    protected final String shortBranch;
    protected final String randomId;
    protected final String stage;


    public CloudFormationConfigurable(String projectId, String branchName) {
        this.projectId = projectId;
        this.branchName = setBranchName(branchName);
        this.shortBranch=shortBranch(branchName);
        this.stage = testOrProd();
        this.randomId = DigestUtils.sha1Hex(branchName);
    }

    private String setBranchName(String branchName) {
        return branchName;
    }

    private String shortBranch(String branchName){
        int cutIndex=Math.min(branchName.length(),maxBranchNameLength);
        return branchName.substring(cutIndex);
    }


    public String testOrProd() {
        if (branchName.equalsIgnoreCase(MASTER_BRANCH)) {
            return "prod";
        } else {
            return "dev";
        }
    }


    public String format(String... args) {
        return String.join("-", args);

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
