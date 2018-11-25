package no.bibsys.cloudformation;


import no.bibsys.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public   class CloudFormationConfigurable {

    private final static Logger logger = LoggerFactory.getLogger(CloudFormationConfigurable.class);

    public final static int NORMALIZED_BRANCH_MAX_LENGTH = 25;
    public final static transient int MAX_BRANCH_WORD_LENGTH = 5;
    public final static transient int MAX_PROJECT_WORD_LENGTH = 3;

    protected final transient String projectId;
    protected final transient String normalizedBranchName;
    private final transient StringUtils stringUtils = new StringUtils();
    private final transient String branchName;


    public CloudFormationConfigurable(String repositoryName, String branchName) {
        this.projectId = initProjectId(repositoryName);
        this.branchName = branchName;
        this.normalizedBranchName = initNormalizedBranchName(branchName);
    }

    private String initProjectId(String repositoryName) {
        String projectName = stringUtils
            .shortNormalizedString(repositoryName, MAX_PROJECT_WORD_LENGTH);
        if (logger.isInfoEnabled()) {
            logger.info("PROJECT NAME IS:{}", projectName);
        }
        return projectName;
    }


    private String initNormalizedBranchName(String branchName) {
        String normalized = stringUtils.shortNormalizedString(branchName, MAX_BRANCH_WORD_LENGTH);
        int cutIndex = Math.min(normalized.length(), NORMALIZED_BRANCH_MAX_LENGTH);
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
