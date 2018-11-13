package no.bibsys.cloudformation;

public class CodeBuildConfiguration extends CloudFormationConfigurable {

    private final String buildProjectName;
    private final String outputArtifact;



    public CodeBuildConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);
        this.buildProjectName = initBuildProjectName();
        this.outputArtifact = initCodeBuildArtifact();

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
