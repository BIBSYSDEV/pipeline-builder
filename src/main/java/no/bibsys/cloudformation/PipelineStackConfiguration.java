package no.bibsys.cloudformation;

public class PipelineStackConfiguration extends CloudFormationConfigurable {

    private final String pipelineStackName;

    // Role for creating the stack of the pipeline
    private final String createStackRoleName;

    // Role for executing the steps of the pipeline
    private final String pipelineRoleName;

    private String bucketName;
    private GithubConf githubConf;

    private PipelineConfiguration pipelineConfiguration;
    private CodeBuildConfiguration codeBuildConfiguration;


    public PipelineStackConfiguration(String projectName, String branchName) {
        super(projectName, branchName);
        this.pipelineStackName = pipelineStackName();
        this.bucketName = initBucketName();
        this.createStackRoleName = initCreateStackRole();
        this.pipelineRoleName = initPipelineRoleName();

        this.githubConf = new GithubConf();
        this.pipelineConfiguration = new PipelineConfiguration(projectName, branchName);
        this.codeBuildConfiguration = new CodeBuildConfiguration(projectName, branchName);
    }


    private String initCreateStackRole() {
        return format("CreateStack", randomId);
    }

    private String initBucketName() {
        return format(projectId, shortBranch);
    }


    public String getBucketName() {
        return bucketName;
    }


    public String getPipelineStackName() {
        return pipelineStackName;
    }


    private String pipelineStackName() {
        return format(projectId, branchName, "pipelineStack");
    }


    private String initPipelineRoleName() {
        return format("PipelineRole", getRandomId());
    }


    public String getPipelineRoleName() {
        return pipelineRoleName;
    }


    public String getCreateStackRoleName() {
        return createStackRoleName;
    }


    public GithubConf getGithubConf() {
        return githubConf;
    }

    public PipelineConfiguration getPipelineConfiguration() {
        return pipelineConfiguration;
    }

    public CodeBuildConfiguration getCodeBuildConfiguration() {
        return codeBuildConfiguration;
    }


}
