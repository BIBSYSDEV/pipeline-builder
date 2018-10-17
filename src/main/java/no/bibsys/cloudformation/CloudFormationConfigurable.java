package no.bibsys.cloudformation;


import no.bibsys.utils.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

public class CloudFormationConfigurable {

    private static final int maxBranchNameLength = 12;
    private final transient StringUtils stringUtils=new StringUtils();

    protected final transient String projectId;
    protected final transient String branchName;
    protected final transient String shortBranch;


    public CloudFormationConfigurable(String projectId, String branchName) {
        this.projectId = projectId;
        this.branchName = initBranchName(branchName);
        this.shortBranch = initShortBranch(branchName);
    }

    private String initBranchName(String branchName) {
        return branchName;
    }

    private String initShortBranch(String branchName) {
        String normalized=stringUtils.shortNormalizedString(branchName);
       return normalized;
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





    public String getShortBranch() {
        return shortBranch;
    }
}
