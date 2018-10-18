package no.bibsys.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigurable {


    private final String sourceOutputArtifactName;
    private final String testServiceStack;
    private final String finalServiceStack;
    private final String pipelineName;


    private final String lambdaTrustRolename;


    public PipelineConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);

        this.sourceOutputArtifactName = initSourceOutputArtifactName();
        this.testServiceStack = initServiceStack("test");
        this.finalServiceStack = initServiceStack("prod");
        this.pipelineName = initializePipelineName();
        this.lambdaTrustRolename = initializeLambdaTrustRole();
    }

    private String initializeLambdaTrustRole() {
        return format("LambdaTrustRole", projectId, normalizedBranchName);
    }

    private String initializePipelineName() {
        return format(projectId, normalizedBranchName, "pipeline");
    }

    private String initServiceStack(String postifx) {
        return format(projectId, normalizedBranchName, "serviceStack", postifx);
    }

    private String initSourceOutputArtifactName() {
        return format(projectId, normalizedBranchName, "sourceOutput");
    }


    public String getPipelineName() {
        return pipelineName;
    }


    public String getSourceOutputArtifactName() {
        return sourceOutputArtifactName;
    }


    public String getTestServiceStack() {
        return testServiceStack;
    }

    public String getFinalServiceStack() {
        return finalServiceStack;
    }


    public String getLambdaTrustRolename() {
        return lambdaTrustRolename;
    }


}
