package no.bibsys.cloudformation;

public class PipelineConfiguration extends CloudFormationConfigrable {

    private String bucketName;
    private String roleName;
    private String sourceOutputArtifactName;


    public PipelineConfiguration(String projectId, String branchName) {
        super(projectId,branchName);
        this.bucketName = initBucketName();
        this.roleName=initRoleName();
        this.sourceOutputArtifactName=initSourceOutputArtifactName();
    }

    private String initSourceOutputArtifactName() {
        return format(projectId,branchName,"source-output");
    }

    private String initRoleName() {
        return  format(projectId,branchName);
    }

    private String initBucketName() {
        String postfix=devOrProd();
        return format(projectId,postfix);
    }


    public String getBucketName() {
        return bucketName;
    }

    public String getRoleName() {
        return roleName;
    }


    public String getSourceOutputArtifactName() {
        return sourceOutputArtifactName;
    }


}
