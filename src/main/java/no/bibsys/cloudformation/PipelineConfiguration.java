package no.bibsys.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigurable {


    private final String sourceOutputArtifactName;
    private final String testServiceStack;
    private final String finalServiceStack;
    private final String pipelineName;



    private final String initLambdaFunctionName;


    public PipelineConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);

        this.sourceOutputArtifactName = initSourceOutputArtifactName();
        this.testServiceStack = initServiceStack(Stage.TEST);
        this.finalServiceStack = initServiceStack(Stage.FINAL);
        this.pipelineName = initializePipelineName();

        initLambdaFunctionName = initInitLambdaFunction();
    }

    private String initInitLambdaFunction() {
        return format(projectId,normalizedBranchName,"init-function");
    }


    private String initializePipelineName() {
        return format(projectId, normalizedBranchName, "pipeline");
    }

    private String initServiceStack(String postifx) {
        return format(projectId, normalizedBranchName, "service-stack", postifx);
    }

    private String initSourceOutputArtifactName() {
        return format(projectId, normalizedBranchName, "sourceOutput");
    }

    public String getInitLambdaFunctionName() {
        return initLambdaFunctionName;
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




}
