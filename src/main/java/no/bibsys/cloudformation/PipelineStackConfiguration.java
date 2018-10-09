package no.bibsys.cloudformation;

public class PipelineStackConfiguration extends CloudFormationConfigurable {

    private final String pipelineStackName;
    private final CloudFormationTemplateParameters cloudFormationParameters;
    private String bucketName;
    private final String pipelineRoleName;
    private final String createStackRole;

    public PipelineStackConfiguration(String projectName, String branchName) {
        super(projectName,branchName);
        this.pipelineStackName =pipelineStackName();
        this.cloudFormationParameters=
            new CloudFormationTemplateParameters(projectName,branchName);
        this.bucketName=initBucketName();
        this.pipelineRoleName = initPipelineRoleName();
        this.createStackRole=initCreateStackRole();
    }



    private String initBucketName() {
        return format(projectId,branchName);
    }


    public String getBucketName() {
        return bucketName;
    }


    public String getPipelineStackName() {
        return pipelineStackName;
    }


    private String pipelineStackName(){
        return format(projectId,branchName,"pipelineStack");
    }

    public GithubConf getGithubConf() {
        return this.cloudFormationParameters.getGithubConf();
    }

    public PipelineConfiguration getPipelineConfiguration() {
        return  this.cloudFormationParameters.getPipelineConfiguration();
    }

    public CodeBuildConfiguration getCodeBuildConfiguration() {

        return this.cloudFormationParameters.getCodeBuildConfiguration();
    }


    private String initPipelineRoleName() {
        return  format("PipelineRole",getRandomId());
    }



    public String getPipelineRoleName() {
        return pipelineRoleName;
    }



    private String initCreateStackRole() {
        return  format(projectId,branchName,"create-stack");
    }



    public String getCreateStackRole() {
        return createStackRole;
    }


}
