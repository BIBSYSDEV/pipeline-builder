package no.bibsys.aws.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigurable {

    public static final String DESTROY_FUNCTION_SUFFIX = "destroy-function";
    public static final String INIT_FUNCTION_SUFFIX = "init-function";
    public static final String PIPELINE_NAME_SUFFIX = "pipeline";
    public static final String SERVICE_STACK_SUFFIX = "service-stack";
    public static final String SOURCE_OUTPUT_SUFFIX = "sourceOutput";
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
        return format(projectId, normalizedBranchName, DESTROY_FUNCTION_SUFFIX);
    }

    private String initInitLambdaFunction() {
        return format(projectId, normalizedBranchName, INIT_FUNCTION_SUFFIX);
    }


    private String initializePipelineName() {
        return format(projectId, normalizedBranchName, PIPELINE_NAME_SUFFIX);
    }

    private String initServiceStack(Stage stage) {
        return format(projectId, normalizedBranchName, SERVICE_STACK_SUFFIX, stage.toString());
    }

    private String initSourceOutputArtifactName() {
        return format(projectId, normalizedBranchName, SOURCE_OUTPUT_SUFFIX);
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
