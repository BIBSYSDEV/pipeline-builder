package no.bibsys.roles;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyResult;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.bibsys.cloudformation.PipelineConfiguration;

public class RoleManager {


    private final transient String assumePolicy;
    private final transient String policy;
    private final transient AmazonIdentityManagement iam;
    private final transient String roleName;


    public RoleManager(PipelineConfiguration configuration) {
        this.roleName = configuration.getLambdaTrustRolename();
        this.assumePolicy = configuration.getLambdaTrustRoleAssumePolicy();
        this.policy = configuration.getLambdaTrustRoleAccessPolicy();
        this.iam = AmazonIdentityManagementClientBuilder.defaultClient();
    }


    public Role createRole() {

        CreateRoleRequest createRoleRequest = new CreateRoleRequest();
        createRoleRequest
            .withPath("/")
            .withRoleName(roleName)
            .withAssumeRolePolicyDocument(assumePolicy);

        Role role = iam.createRole(createRoleRequest).getRole();
        waitForRole();
        putRolePolicy();

        return role;
    }

    private void putRolePolicy() {
        //add inline policy
        PutRolePolicyRequest putRolePolicyRequest = new PutRolePolicyRequest()
            .withPolicyDocument(policy).withPolicyName("accessPolicy")
            .withRoleName(roleName);

        iam.putRolePolicy(putRolePolicyRequest);
    }


    public void updateRole(){
        if(getRole().isPresent()) {
            deleteInlineRolePolicies();
            putRolePolicy();
        }
        else {
            createRole();
        }

    }


    public void deleteRole() {
        if (getRole().isPresent()) {
            deleteInlineRolePolicies();
            iam.deleteRole(new DeleteRoleRequest().withRoleName(roleName));
        }
    }


    public List<String> listInlinePolicies() throws UnsupportedEncodingException {
        List<String> policyNames = iam
            .listRolePolicies(new ListRolePoliciesRequest().withRoleName(roleName))
            .getPolicyNames();
        List<String> urlEncodedPolicyDocuments = policyNames.stream()
            .map(this::getInlinePolicy)
            .map(GetRolePolicyResult::getPolicyDocument)
            .collect(Collectors.toList());
        List<String> policyDocuments=new ArrayList<>();
        for(String encodedString:urlEncodedPolicyDocuments){
            policyDocuments.add(URLDecoder.decode(encodedString,StandardCharsets.UTF_8.toString()));
        }

        return policyDocuments;

    }

    private GetRolePolicyResult getInlinePolicy(String policyName) {
        return iam.getRolePolicy(new GetRolePolicyRequest().withPolicyName(policyName).withRoleName(roleName));
    }


    private void deleteInlineRolePolicies() {
        List<DeleteRolePolicyRequest> inlinePolicies = iam
            .listRolePolicies(new ListRolePoliciesRequest().withRoleName(roleName))
            .getPolicyNames()
            .stream()
            .map(policyName -> new DeleteRolePolicyRequest().withRoleName(roleName)
                .withPolicyName(policyName)).collect(Collectors.toList());

        inlinePolicies.forEach(iam::deleteRolePolicy);
    }

    public Optional<Role> getRole() {
        try {
            Role role = iam.getRole(new GetRoleRequest().withRoleName(roleName)).getRole();
            return Optional.of(role);
        } catch (NoSuchEntityException e) {
            return Optional.empty();
        }

    }


    private void waitForRole() {

        Role role = iam
            .getRole(new GetRoleRequest().withRoleName(roleName)).getRole();
        while (role == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                waitForRole();
            }
            waitForRole();

        }


    }


    public String getAssumePolicy() {
        return assumePolicy;
    }


    public String getPolicy() {
        return policy;
    }


}
