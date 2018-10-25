package no.bibsys.cloudformation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubReader;
import no.bibsys.utils.JsonUtils;

public class PipelineStackConfiguration extends CloudFormationConfigurable {


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


    public PipelineStackConfiguration(GithubReader githubreader) throws IOException {
        super(githubreader.getGitInfo().getRepo(), githubreader.getBranch());
        this.githubConf = githubreader.getGitInfo();
        this.pipelineStackName = initPipelineStackName();
        this.bucketName = initBucketName();
        this.createStackRoleName = initCreateStackRole();
        this.pipelineRoleName = initPipelineRoleName();

        this.pipelineConfiguration = initPipelineConfiguration(githubreader.getBranch(),
            githubConf.getRepo(), githubreader);
        this.codeBuildConfiguration = new CodeBuildConfiguration(githubConf.getRepo(),
            githubreader.getBranch());
    }

    private PipelineConfiguration initPipelineConfiguration(String branchName, String repoName,
        GithubReader githubReader)
        throws IOException {
        PolicyReader policyReader = new PolicyReader(githubReader).invoke();
        String assumePolicyDocument = policyReader.getAssumePolicyDocument();
        String accessPolicyDocument = policyReader.getAccessPolicyDocument();

        return new PipelineConfiguration(repoName, branchName, assumePolicyDocument,
            accessPolicyDocument);
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


    private class PolicyReader {


        private final transient Config config = ConfigFactory.load().resolve();
        private final transient GithubReader githubReader;
        private transient String assumePolicyDocument;
        private transient String accessPolicyDocument;

        public PolicyReader(GithubReader githubReader) {
            this.githubReader = githubReader;
        }

        public String getAssumePolicyDocument() {
            return assumePolicyDocument;
        }

        public String getAccessPolicyDocument() {
            return accessPolicyDocument;
        }

        public PolicyReader invoke() throws IOException {

            String folder = config.getString("policies.parentFolder");
            String assumePolicy = config.getString("policies.assumePolicyFile");
            String accessPolicy = config.getString("policies.accessPolicyFile");
            Path assumePolicyPath = Paths.get(folder, assumePolicy);
            Path accessPolicyPath = Paths.get(folder, accessPolicy);

            assumePolicyDocument = JsonUtils
                .removeComments(githubReader.readFile(assumePolicyPath));
            accessPolicyDocument = JsonUtils
                .removeComments(githubReader.readFile(accessPolicyPath));
            return this;
        }
    }
}
