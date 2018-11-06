package no.bibsys.cloudformation;

public class CodeBuildConfiguration extends CloudFormationConfigurable {

    private final String buildProjectName;
    private final String outputArtifact;



    public CodeBuildConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);
        this.buildProjectName = format(projectId, normalizedBranchName);
        this.outputArtifact = format(projectId, normalizedBranchName, "codeBuildArtifact");

    }


    public String getBuildProjectName() {
        return buildProjectName;
    }

    public String getOutputArtifact() {
        return outputArtifact;
    }


}
