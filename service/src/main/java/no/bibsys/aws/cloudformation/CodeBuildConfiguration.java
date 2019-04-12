package no.bibsys.aws.cloudformation;

public class CodeBuildConfiguration extends Configurable {

    private final String buildProjectName;
    private final String outputArtifact;
    private final String executeTestsProjectName;



    public String getExecuteTestsProjectName() {
        return executeTestsProjectName;
    }

    public CodeBuildConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);
        this.buildProjectName = initBuildProjectName();
        this.outputArtifact = initCodeBuildArtifact();
        this.executeTestsProjectName = initExecuteTestsProjectName();

    }

    private String initExecuteTestsProjectName() {
        return format(projectId, normalizedBranchName, "executeTests");
    }

    private String initCodeBuildArtifact() {
        return format(projectId, normalizedBranchName, "codeBuildArtifact");
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


}
