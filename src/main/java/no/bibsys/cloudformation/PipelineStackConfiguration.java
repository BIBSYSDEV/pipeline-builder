package no.bibsys.cloudformation;

public class PipelineStackConfiguration extends CloudFormationConfigrable{

    private final String name;
    private final CloudFormationTemplateParameters cloudFormationParameters;

    public PipelineStackConfiguration(String projectName, String branchName) {
        super(projectName,branchName);
        this.name=pipelineStackName();
        this.cloudFormationParameters=
            new CloudFormationTemplateParameters(projectName,branchName);
    }



    public String getName() {
        return name;
    }


    private String pipelineStackName(){
        return format(projectId,branchName);
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


}
