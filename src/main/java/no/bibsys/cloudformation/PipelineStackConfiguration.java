package no.bibsys.cloudformation;

public class PipelineStackConfiguration extends CloudFormationConfigurable {

    private final String pipelineStackName;
    private String bucketName;
    private final String pipelineRoleName;
    private final String createStackRoleName;
    private final CloudFormationTemplateParameters cloudFormationParameters;



    public PipelineStackConfiguration(String projectName, String branchName) {
        super(projectName,branchName);
        this.pipelineStackName =pipelineStackName();
        this.cloudFormationParameters=
            new CloudFormationTemplateParameters(projectName,branchName);
        this.bucketName=initBucketName();
        this.pipelineRoleName = initPipelineRoleName();
        this.createStackRoleName=initCreateStackRole();
    }


    private String initCreateStackRole() {
        return  format("CreateStack",randomId);
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
        return format(projectId,shortBranch,"pipelineStack");
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





    public String getCreateStackRoleName() {
        return createStackRoleName;
    }


}
