package no.bibsys.aws.cloudformation;

public class CodeBuildConfiguration extends CloudFormationConfigurable {

    private final String buildProjectName;
    private final String outputArtifact;
    private final String cypressTestsProjectName;



    public String getCypressTestsProjectName() {
        return cypressTestsProjectName;
    }

    public CodeBuildConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);
        this.buildProjectName = initBuildProjectName();
        this.outputArtifact = initCodeBuildArtifact();
        this.cypressTestsProjectName = initCypressTestsProjectName();

    }

    private String initCypressTestsProjectName() {
        return format(projectId, normalizedBranchName, "cypressTests");
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
