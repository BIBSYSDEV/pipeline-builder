package no.bibsys.cloudformation;

public class CloudFormationTemplateParameters extends CloudFormationConfigrable {


    private GithubConf githubConf;
    private PipelineConfiguration pipelineConfiguration;



    private CodeBuildConfiguration codeBuildConfiguration;

    public CloudFormationTemplateParameters(
        String projectName,
        String branchName
    ) {
        super(projectName, branchName);
        this.githubConf = new GithubConf();
        this.pipelineConfiguration = new PipelineConfiguration(projectName, branchName);
        this.codeBuildConfiguration=new CodeBuildConfiguration(projectName,branchName);

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
