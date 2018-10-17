package no.bibsys.cloudformation;

public class CodeBuildConfiguration extends CloudFormationConfigurable {

    private final String buildProjectName;
    private final String outputArtifact;
    private final String cacheBucket;


    public CodeBuildConfiguration(String projectId, String branchName) {
        super(projectId, branchName);
        this.buildProjectName = format(projectId, branchName);
        this.outputArtifact = format(projectId, shortBranch, "codeBuildArtifact");
        this.cacheBucket = initCacheBucket();
    }

    private String initCacheBucket() {
        String bucketName = format(projectId,shortBranch, "buildcache");
        return bucketName;
    }

    public String getBuildProjectName() {
        return buildProjectName;
    }

    public String getOutputArtifact() {
        return outputArtifact;
    }


    public String getCacheBucket() {
        return cacheBucket;
    }
}
