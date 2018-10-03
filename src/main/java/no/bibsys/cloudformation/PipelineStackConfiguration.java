package no.bibsys.cloudformation;

public class PipelineStackConfiguration extends CloudFormationConfigrable{

    private final String pipelineStackName;
    private final CloudFormationTemplateParameters cloudFormationParameters;
    private String bucketName;
    private final String pipelineRole;
    private final String createStackRole;

    public PipelineStackConfiguration(String projectName, String branchName) {
        super(projectName,branchName);
        this.pipelineStackName =pipelineStackName();
        this.cloudFormationParameters=
            new CloudFormationTemplateParameters(projectName,branchName);
        this.bucketName=initBucketName();
        this.pipelineRole = initPipelineRoleName();
        this.createStackRole=initCreateStackRole();
    }



    private String initBucketName() {
        String postfix=devOrProd();
        return format(projectId,postfix);
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
        return  format(projectId,branchName,"build-pipeline-role");
    }



    public String getPipelineRole() {
        return pipelineRole;
    }



    private String initCreateStackRole() {
        return  format(projectId,branchName,"create-stack");
    }



    public String getCreateStackRole() {
        return createStackRole;
    }


}
