package no.bibsys.cloudFormation;

public class CloudFormationParameters extends CloudFormationConfigrable{

    private GithubConf githubConf;
    private PipelineConfiguration pipelineConfiguration;


    public CloudFormationParameters(GithubConf githubConf,
        String projectName,
        String branchName
    ) {
        super(projectName,branchName);
        this.githubConf = githubConf;
        pipelineConfiguration = new PipelineConfiguration(projectName,branchName);

    }


}
