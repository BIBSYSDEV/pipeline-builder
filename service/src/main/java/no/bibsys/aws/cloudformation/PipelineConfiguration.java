package no.bibsys.aws.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigurable {


    private final String sourceOutputArtifactName;
    private final String testServiceStack;
    private final String finalServiceStack;
    private final String pipelineName;

    private final String initLambdaFunctionName;



    private final String destroyLambdaFunctionName;


    public PipelineConfiguration(String repositoryName, String branchName) {
        super(repositoryName, branchName);

        this.sourceOutputArtifactName = initSourceOutputArtifactName();
        this.testServiceStack = initServiceStack(Stage.TEST);
        this.finalServiceStack = initServiceStack(Stage.FINAL);
        this.pipelineName = initializePipelineName();

        initLambdaFunctionName = initInitLambdaFunction();
        destroyLambdaFunctionName = initDestroyLambdaFunction();
    }

    private String initDestroyLambdaFunction() {
        return format(projectId, normalizedBranchName, "destroy-function");
    }

    private String initInitLambdaFunction() {
        return format(projectId, normalizedBranchName, "init-function");
    }


    private String initializePipelineName() {
        return format(projectId, normalizedBranchName, "pipeline");
    }

    private String initServiceStack(Stage stage) {
        return format(projectId, normalizedBranchName, "service-stack", stage.toString());
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

    public String getDestroyLambdaFunctionName() {
        return destroyLambdaFunctionName;
    }


    public String getCurrentServiceStackName(Stage stage) {
        if (stage.equals(Stage.FINAL)) {
            return getFinalServiceStack();
        } else if (stage.equals(Stage.TEST)) {
            return getTestServiceStack();
        } else {
            throw new IllegalStateException("Invalid Stage:" + stage.name());
        }

    }


}
