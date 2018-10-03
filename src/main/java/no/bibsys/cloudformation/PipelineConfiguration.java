package no.bibsys.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigrable {


    private  final String sourceOutputArtifactName;
    private final String testStackName;


    private final String pipelineName;


    public PipelineConfiguration(String projectId, String branchName) {
        super(projectId,branchName);

        this.sourceOutputArtifactName=initSourceOutputArtifactName();
        this.testStackName=initTestStackName();
        this. pipelineName = initializePipelineName();
    }

    private String initializePipelineName() {
        return format(projectId,branchName,"pipeline");
    }

    private String initTestStackName() {
        return format(projectId,branchName,"testStack");
    }

    private String initSourceOutputArtifactName() {
        return format(projectId,branchName,"sourceOutput");
    }


    public String getPipelineName() {
        return pipelineName;
    }


    public String getSourceOutputArtifactName() {
        return sourceOutputArtifactName;
    }

    public String getTestStackName() {
        return testStackName;
    }


}
