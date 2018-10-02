package no.bibsys.cloudFormation;

public class PipelineConfiguration extends CloudFormationConfigrable {

    private String bucketName;
    private String roleName;



    public PipelineConfiguration(String projectName, String branchName) {
        super(projectName,branchName);
        this.bucketName = initBucketName();
        this.roleName=initRoleName();
    }

    private String initRoleName() {
        return  format(projectName,branchName);
    }

    private String initBucketName() {
        String postfix=devOrProd();
        return format(projectName,postfix);
    }


    public String getBucketName() {
        return bucketName;
    }

    public String getRoleName() {
        return roleName;
    }


}
