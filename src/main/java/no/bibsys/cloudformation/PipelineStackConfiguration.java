package no.bibsys.cloudformation;

import java.io.IOException;
import no.bibsys.git.github.GithubConf;
import no.bibsys.utils.Environment;

public class PipelineStackConfiguration extends CloudFormationConfigurable {

    private final transient String pipelineStackName;

    // Role for creating the stack of the pipeline
    private final transient String createStackRoleName;

    // Role for executing the steps of the pipeline
    private final transient String pipelineRoleName;

    private final transient String bucketName;
    private final transient GithubConf githubConf;

    private final transient PipelineConfiguration pipelineConfiguration;
    private final transient CodeBuildConfiguration codeBuildConfiguration;


    public PipelineStackConfiguration(String branchName,
        String repoName,
        String repoOwner,
        Environment environment) throws IOException {
        super(repoName, branchName);
        this.pipelineStackName = initPipelineStackName();
        this.bucketName = initBucketName();
        this.createStackRoleName = initCreateStackRole();
        this.pipelineRoleName = initPipelineRoleName();

        this.githubConf = new GithubConf(repoOwner, repoName, environment);
        this.pipelineConfiguration = new PipelineConfiguration(repoName, branchName);
        this.codeBuildConfiguration = new CodeBuildConfiguration(repoName, branchName);
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
