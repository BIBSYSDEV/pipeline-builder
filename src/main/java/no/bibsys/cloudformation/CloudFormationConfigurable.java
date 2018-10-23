package no.bibsys.cloudformation;


import no.bibsys.utils.StringUtils;

public class CloudFormationConfigurable {

    public final static int NORMALIZED_BRANCH_MAX_LENGTH = 40;
    public final static transient int MAX_BRANCH_WORD_LENGTH = 10;
    public final static transient int MAX_PROJECT_WORD_LENGTH = 3;

    private final transient StringUtils stringUtils = new StringUtils();

    protected final transient String projectId;
    private final transient String branchName;
    protected final transient String normalizedBranchName;


    public CloudFormationConfigurable(String repositoryName, String branchName) {
        this.projectId = initProjectId(repositoryName);
        this.branchName = initBranchName(branchName);
        this.normalizedBranchName = initShortBranch(branchName);
    }

    private String initProjectId(String repositoryName) {
        String projectName = stringUtils
            .shortNormalizedString(repositoryName, MAX_PROJECT_WORD_LENGTH);
        System.out.println("PROJECT NAME IS:" + projectName);
        return projectName;
    }

    private String initBranchName(String branchName) {
        return branchName;
    }

    private String initShortBranch(String branchName) {
        String normalized = stringUtils.shortNormalizedString(branchName, MAX_BRANCH_WORD_LENGTH);
        int cutIndex=Math.min(normalized.length(),NORMALIZED_BRANCH_MAX_LENGTH);
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


    public String getNormalizedBranchName() {
        return normalizedBranchName;
    }
}
