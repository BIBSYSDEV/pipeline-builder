package no.bibsys.cloudformation;


import no.bibsys.utils.StringUtils;

public class CloudFormationConfigurable {


    private final transient StringUtils stringUtils=new StringUtils();

    protected final transient String projectId;
    protected final transient String branchName;
    protected final transient String normalizedBranchName;


    public CloudFormationConfigurable(String repositoryName, String branchName) {
        this.projectId = initProjectId(repositoryName);
        this.branchName = initBranchName(branchName);
        this.normalizedBranchName = initShortBranch(branchName);
    }

    private String initProjectId(String repositoryName) {

        String projectName=stringUtils.shortNormalizedString(repositoryName);
        System.out.println("PROJECT NAME IS:"+projectName);
        return projectName;
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


    public String getNormalizedBranchName() {
        return normalizedBranchName;
    }
}
