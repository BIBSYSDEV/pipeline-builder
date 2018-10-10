package no.bibsys.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigurable {


    private  final String sourceOutputArtifactName;
    private final String serviceStack;
    private final String pipelineName;


    private final String lambdaTrustRolename;



    public PipelineConfiguration(String projectId, String branchName) {
        super(projectId,branchName);

        this.sourceOutputArtifactName=initSourceOutputArtifactName();
        this.serviceStack = initServiceStack();
        this. pipelineName = initializePipelineName();
        this.lambdaTrustRolename =initializeLambdaTrustRole();
    }

    private String initializeLambdaTrustRole() {
        return format("LambdaTrustRole",randomId);
    }

    private String initializePipelineName() {
        return format(projectId,shortBranch,"pipeline");
    }

    private String initServiceStack() {
        return format(projectId,shortBranch,"serviceStack");
    }

    private String initSourceOutputArtifactName() {
        return format(projectId,shortBranch,"sourceOutput");
    }


    public String getPipelineName() {
        return pipelineName;
    }


    public String getSourceOutputArtifactName() {
        return sourceOutputArtifactName;
    }

    public String getServiceStack() {
        return serviceStack;
    }


    public String getLambdaTrustRolename() {
        return lambdaTrustRolename;
    }


}
