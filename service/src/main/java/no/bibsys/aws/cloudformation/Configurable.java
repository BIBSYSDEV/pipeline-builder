package no.bibsys.aws.cloudformation;

import no.bibsys.aws.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with basic information about the project and the branch. * See also {@link
 * PipelineStackConfiguration}, {@link CodeBuildConfiguration}, and {@link PipelineConfiguration}
 */
public class Configurable {

    public static final int NORMALIZED_BRANCH_MAX_LENGTH = 25;
    private static final int MAX_BRANCH_WORD_LENGTH = 6;
    public static final int MAX_PROJECT_WORD_LENGTH = 3;
    private static final Logger logger = LoggerFactory.getLogger(Configurable.class);
    protected final transient String projectId;
    protected final transient String normalizedBranchName;
    private final transient StringUtils stringUtils = new StringUtils();
    private final transient String branchName;

    public Configurable(String repositoryName, String branchName) {
        this.projectId = initProjectId(repositoryName);
        this.branchName = branchName;
        this.normalizedBranchName = initNormalizedBranchName(branchName);
    }

    private String initProjectId(String repositoryName) {
        String projectName = stringUtils
            .shortNormalizedString(repositoryName, MAX_PROJECT_WORD_LENGTH);
        logger.info("PROJECT NAME IS:" + projectName);
        return projectName;
    }

    private String initNormalizedBranchName(String branchName) {
        String normalized = stringUtils.shortNormalizedString(branchName, MAX_BRANCH_WORD_LENGTH);
        int cutIndex = Math.min(normalized.length(), NORMALIZED_BRANCH_MAX_LENGTH);
        String result = normalized.substring(0, cutIndex);
        result = removeNonAlpahnumbericCharsFromEnd(result);
        return result;
    }

    private String removeNonAlpahnumbericCharsFromEnd(String input) {
        char lastChar = input.charAt(input.length() - 1);
        if (Character.isAlphabetic(lastChar) || Character.isDigit(lastChar)) {
            return input;
        } else {
            return removeNonAlpahnumbericCharsFromEnd(input.substring(0, input.length() - 1));
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

    public String getNormalizedBranchName() {
        return normalizedBranchName;
    }
}
