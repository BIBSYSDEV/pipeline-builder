package no.bibsys.cloudformation;

public class CodeBuildConfiguration extends CloudFormationConfigurable {

    private String projectName;
    private String outputArtifact;



    public CodeBuildConfiguration(String projectId, String branchName) {
        super(projectId, branchName);
        this.projectName=format(projectId,branchName);
        this.outputArtifact=format(projectId,shortBranch,"codeBuildArtifact");
    }

    public String getProjectName() {
        return projectName;
    }

    public String getOutputArtifact() {
        return outputArtifact;
    }

}
