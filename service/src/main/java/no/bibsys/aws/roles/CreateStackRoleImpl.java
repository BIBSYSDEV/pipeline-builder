package no.bibsys.aws.roles;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleResult;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.utils.github.GithubReader;
import no.bibsys.aws.utils.github.NotFoundException;
import no.bibsys.aws.utils.github.UnauthorizedException;

public class CreateStackRoleImpl implements CreateStackRole {

    private static final String TEMPLATES_DIRECTORY = "templates";
    private static final String CREATE_STACK_ROLE_ASSUME_POLICY_JSON = "createStackRoleAssumePolicy.json";
    private static final String CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON = "createStackRolePolicyDocument.json";
    private final transient GithubReader githubReader;
    private final transient PipelineStackConfiguration pipelineStackConfiguration;
    private final transient AmazonIdentityManagement amazonIdentityManagement;

    public CreateStackRoleImpl(GithubReader githubReader,
        PipelineStackConfiguration pipelineStackConfiguration,
        AmazonIdentityManagement amazonIdentityManagement) {
        this.githubReader = githubReader;
        this.pipelineStackConfiguration = pipelineStackConfiguration;
        this.amazonIdentityManagement = amazonIdentityManagement;
    }

    @Override
    public AttachRolePolicyRequest createNewAttachPolicyRequest(String policyArn, String roleName) {
        return new AttachRolePolicyRequest()
            .withPolicyArn(policyArn)
            .withRoleName(roleName);
    }

    @Override
    public String createRole() throws Exception {
        CreateRoleRequest createRoleRequest = createNewCreateRoleRequest();
        PutRolePolicyRequest putRolePolicyRequest = createNewPutRolePolicyRequest();

        Role role = amazonIdentityManagement.createRole(createRoleRequest).getRole();
        waitForRole();
        amazonIdentityManagement.putRolePolicy(putRolePolicyRequest);

        return role.getRoleName();
    }

    @Override
    public DeleteRoleResult deleteRole() {
        List<DeleteRolePolicyRequest> inlinePolicies = amazonIdentityManagement
            .listRolePolicies(new ListRolePoliciesRequest()
                .withRoleName(pipelineStackConfiguration.getCreateStackRoleName()))
            .getPolicyNames()
            .stream()
            .map(policyName -> new DeleteRolePolicyRequest()
                .withRoleName(pipelineStackConfiguration.getCreateStackRoleName())
                .withPolicyName(policyName)).collect(Collectors.toList());

        inlinePolicies.forEach(amazonIdentityManagement::deleteRolePolicy);
        DeleteRoleResult deleteRoleResult = amazonIdentityManagement.deleteRole(
            new DeleteRoleRequest()
                .withRoleName(pipelineStackConfiguration.getCreateStackRoleName()));

        return deleteRoleResult;
    }

    @Override
    public CreateRoleRequest createNewCreateRoleRequest() throws IOException {
        String assumeRolePolicy = IoUtils.resourceAsString(
            Paths.get(TEMPLATES_DIRECTORY, CREATE_STACK_ROLE_ASSUME_POLICY_JSON));
        return new CreateRoleRequest()
            .withAssumeRolePolicyDocument(assumeRolePolicy)
            .withRoleName(this.pipelineStackConfiguration.getCreateStackRoleName());
    }

    @Override
    public PutRolePolicyRequest createNewPutRolePolicyRequest()
        throws IOException, UnauthorizedException, NotFoundException {
        String createStackRolePolicyDocument = githubReader
            .readFile(Paths.get(CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON));
        return new PutRolePolicyRequest()
            .withPolicyDocument(createStackRolePolicyDocument)
            .withPolicyName(pipelineStackConfiguration.getCreateStackRolePolicyName())
            .withRoleName(pipelineStackConfiguration.getCreateStackRoleName());
    }

    private void waitForRole() {

        GetRoleResult roleResult = amazonIdentityManagement
            .getRole(new GetRoleRequest()
                .withRoleName(pipelineStackConfiguration.getCreateStackRoleName()));

        Role role = roleResult.getRole();

        while (role == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                waitForRole();
            }
            waitForRole();
        }
    }
}
