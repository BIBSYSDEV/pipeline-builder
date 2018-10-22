package no.bibsys.cloudformation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import no.bibsys.git.github.GithubConf;
import no.bibsys.git.github.GithubReader;
import no.bibsys.utils.Environment;

public class PipelineStackConfiguration extends CloudFormationConfigurable {


    Config config= ConfigFactory.load().resolve();

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
        this.pipelineConfiguration = initPipelineConfiguration(branchName, repoName);
        this.codeBuildConfiguration = new CodeBuildConfiguration(repoName, branchName);
    }

    private PipelineConfiguration initPipelineConfiguration(String branchName, String repoName)
        throws IOException {
        GithubReader githubReader=new GithubReader(githubConf,branchName);
        String folder=config.getString("policies.parentFolder");
        String assumePolicy=config.getString("policies.assumePolicyFile");
        String accessPolicy=config.getString("policies.accessPolicyFile");
        Path assumePolicyPath=Paths.get(folder,assumePolicy);
        Path accessPolicyPath=Paths.get(folder,accessPolicy);

        String assumePolicyDocument=githubReader.readFile(assumePolicyPath);
        String accessPolicyDocument =githubReader.readFile(accessPolicyPath);

        return new PipelineConfiguration(repoName, branchName,assumePolicyDocument,accessPolicyDocument);
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
