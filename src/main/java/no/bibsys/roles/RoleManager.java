package no.bibsys.roles;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyResult;
import com.amazonaws.services.identitymanagement.model.Role;
import no.bibsys.cloudformation.PipelineConfiguration;

public class RoleManager {


    private transient String assumePolicy;
    private transient String policy;
    private String roleName;


    public RoleManager(PipelineConfiguration configuration) {
        this.roleName = configuration.getLambdaTrustRolename();
    }


    public void createRole() throws InterruptedException {
        AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.defaultClient();
        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest
            .withPath("/")
            .withRoleName(roleName)
            .withAssumeRolePolicyDocument(assumePolicy);

        //add inline policy
        PutRolePolicyRequest putRolePolicyRequest = new PutRolePolicyRequest()
            .withPolicyDocument(policy).withPolicyName("accessRights")
            .withRoleName(roleName);

        Role role = iam
            .createRole(createRoleRequest).getRole();
        waitForRole(iam);

        PutRolePolicyResult result = iam
            .putRolePolicy(putRolePolicyRequest);

    }


    public void deleteRole(){
        AmazonIdentityManagement iam= AmazonIdentityManagementClientBuilder.defaultClient();
        iam.deleteRole(new DeleteRoleRequest().withRoleName(roleName));


    }


    private void waitForRole(AmazonIdentityManagement iam) throws InterruptedException {

        Role role = iam
            .getRole(new GetRoleRequest().withRoleName(roleName)).getRole();
        while(role==null){
            Thread.sleep(1000);
            waitForRole(iam);

        }


    }

    public RoleManager withAssumePolicy(String assumePolicy) {
        this.assumePolicy = assumePolicy;
        return this;
    }

    public RoleManager withPolicy(String policy) {
        this.policy = policy;
        return this;
    }

    public String getAssumePolicy() {
        return assumePolicy;
    }

    public void setAssumePolicy(String assumePolicy) {
        this.assumePolicy = assumePolicy;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
