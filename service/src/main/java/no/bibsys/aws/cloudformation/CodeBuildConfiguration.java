package no.bibsys.aws.cloudformation;

public class CodeBuildConfiguration extends Configurable {

    private static final String EXECUTE_TESTS = "executeTests";
    private static final String CODE_BUILD_ARTIFACT = "codeBuildArtifact";
    private final String buildProjectName;
    private final String outputArtifact;
    private final String executeTestsProjectName;


    public CodeBuildConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);
        this.buildProjectName = initBuildProjectName();
        this.outputArtifact = initCodeBuildArtifact();
        this.executeTestsProjectName = initExecuteTestsProjectName();
    }

    private String initExecuteTestsProjectName() {
        return format(projectId, normalizedBranchName, EXECUTE_TESTS);
    }

    private String initCodeBuildArtifact() {
        return format(projectId, normalizedBranchName, CODE_BUILD_ARTIFACT);
    }

    private String initBuildProjectName() {
        return format(projectId, normalizedBranchName);
    }

    public String getBuildProjectName() {
        return buildProjectName;
    }

    public String getOutputArtifact() {
        return outputArtifact;
    }

    public String getExecuteTestsProjectName() {
        return executeTestsProjectName;
    }

}
