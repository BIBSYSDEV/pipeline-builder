package no.bibsys.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigurable {


    private  final String sourceOutputArtifactName;
    private final String systemStack;

    private final String pipelineName;


    private final String lambdaTrustRolename;



    public PipelineConfiguration(String projectId, String branchName) {
        super(projectId,branchName);

        this.sourceOutputArtifactName=initSourceOutputArtifactName();
        this.systemStack = initSystemStack();
        this. pipelineName = initializePipelineName();
        this.lambdaTrustRolename =initializeLambdaTrustRole();
    }

    private String initializeLambdaTrustRole() {
        return format(projectId,"lambdaRole",randomId);
    }

    private String initializePipelineName() {
        return format(projectId,branchName,"pipeline");
    }

    private String initSystemStack() {
        return format(projectId,branchName,"systemStack");
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

    public String getSystemStack() {
        return systemStack;
    }


    public String getLambdaTrustRolename() {
        return lambdaTrustRolename;
    }


}
