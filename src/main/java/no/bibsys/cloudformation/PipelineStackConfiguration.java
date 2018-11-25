package no.bibsys.cloudformation;

import no.bibsys.git.github.GitInfo;

public class PipelineStackConfiguration extends CloudFormationConfigurable {


    public final static Integer MAX_ROLENAME_SIZE=64;
    private final transient String pipelineStackName;

    // Role for creating the stack of the pipeline
    private final transient String createStackRoleName;

    // Role for executing the steps of the pipeline
    private final transient String pipelineRoleName;

    private final transient String bucketName;
//    private final transient GithubConf githubConf;


    private final GitInfo githubConf;

    private final transient PipelineConfiguration pipelineConfiguration;
    private final transient CodeBuildConfiguration codeBuildConfiguration;


    public PipelineStackConfiguration(GitInfo gitInfo) {
        super(gitInfo.getRepository(), gitInfo.getBranch());
        this.githubConf = gitInfo;
        this.pipelineStackName = initPipelineStackName();
        this.bucketName = initBucketName();
        this.createStackRoleName = initCreateStackRole();
        this.pipelineRoleName = initPipelineRoleName();

        this.pipelineConfiguration = initPipelineConfiguration(gitInfo.getRepository(),
            gitInfo.getBranch());
        this.codeBuildConfiguration = new CodeBuildConfiguration(gitInfo.getRepository(),
            gitInfo.getBranch());
    }


    private PipelineConfiguration initPipelineConfiguration(String repository, String branch) {
        return new PipelineConfiguration(repository, branch);
    }


    private String initCreateStackRole() {
        return format("CreateStack", projectId, normalizedBranchName);
    }

    private String initBucketName() {
        return format(projectId, normalizedBranchName);
    }


    public String getBucketName() {
        return bucketName;
    }


    public String getPipelineStackName() {
        return pipelineStackName;
    }


    private String initPipelineStackName() {
        return format(projectId, normalizedBranchName, "pipelineStack");
    }


    private String initPipelineRoleName() {
        return format("PipelineRole", projectId, normalizedBranchName);
    }


    public String getPipelineRoleName() {
        return pipelineRoleName;
    }


    public String getCreateStackRoleName() {
        return createStackRoleName;
    }


    public PipelineConfiguration getPipelineConfiguration() {
        return pipelineConfiguration;
    }

    public CodeBuildConfiguration getCodeBuildConfiguration() {
        return codeBuildConfiguration;
    }

    public GitInfo getGithubConf() {
        return githubConf;
    }


}
