package no.bibsys.cloudformation;


import no.bibsys.utils.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

public class CloudFormationConfigurable {

    public static final String MASTER_BRANCH = "master";
    private static final int maxBranchNameLength = 12;
    private final transient StringUtils stringUtils=new StringUtils();

    protected final transient String projectId;
    protected final transient String branchName;
    protected final transient String shortBranch;
    protected final transient String randomId;


    public CloudFormationConfigurable(String projectId, String branchName) {
        this.projectId = projectId;
        this.branchName = initBranchName(branchName);
        this.shortBranch = initShortBranch(branchName);
        this.randomId = DigestUtils.sha1Hex(branchName);
    }

    private String initBranchName(String branchName) {
        return branchName;
    }

    private String initShortBranch(String branchName) {
        String normalized=stringUtils.shortNormalizedString(branchName);
        int cutIndex = Math.min(normalized.length(), maxBranchNameLength);
        return normalized.substring(0, cutIndex);
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


    public String getRandomId() {
        return randomId;
    }


    public String getShortBranch() {
        return shortBranch;
    }
}
