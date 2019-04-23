package no.bibsys.aws.cloudformation;

import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.tools.StringUtils;

public class PipelineStackConfiguration extends Configurable {

    public static final Integer MAX_ROLENAME_SIZE = 64;
    public static final Integer BUCKET_NAME_SIZE = 20;
    private static final String POLICY = "policy";
    private static final String CREATE_STACK = "CreateStack";
    private static final String PIPELINE_STACK = "pipelineStack";
    private static final String PIPELINE_ROLE = "PipelineRole";
    private final transient String pipelineStackName;

    // Role for creating the stack of the pipeline
    private final transient String createStackRoleName;

    // Role for executing the steps of the pipeline
    private final transient String pipelineRoleName;

    private final transient String bucketName;
    // private final transient GithubConf githubConf;

    private final GithubConf githubConf;

    private final transient PipelineConfiguration pipelineConfiguration;
    private final transient CodeBuildConfiguration codeBuildConfiguration;

    public PipelineStackConfiguration(GithubConf gitInfo) {
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
        return format(CREATE_STACK, projectId, normalizedBranchName);
    }

    private String initBucketName() {
        return new StringUtils().randomString(BUCKET_NAME_SIZE);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getPipelineStackName() {
        return pipelineStackName;
    }

    private String initPipelineStackName() {
        return format(projectId, normalizedBranchName, PIPELINE_STACK);
    }

    private String initPipelineRoleName() {
        return format(PIPELINE_ROLE, projectId, normalizedBranchName);
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

    public GithubConf getGithubConf() {
        return githubConf;
    }

    public String getCreateStackRolePolicyName() {
        return format(createStackRoleName, POLICY);
    }
}
