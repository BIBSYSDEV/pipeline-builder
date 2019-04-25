package no.bibsys.aws.roles;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleResult;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Role;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteRoleHelper {

    private static final Logger logger = LoggerFactory.getLogger(DeleteRoleHelper.class);
    private static final String NON_EXISTING_ROLE_WARNING_MESSAGE = "Attempting to delete non existing role with name"
        + " {}";
    private static final String NON_EXISTING_ROLE_POLICY_WARNING_MESSAGE =
        "Attempting to delete inline policies for non existing role with name"
            + " {}";

    private final transient AmazonIdentityManagement amazonIdentityManagement;

    public DeleteRoleHelper(
        AmazonIdentityManagement amazonIdentityManagement) {
        this.amazonIdentityManagement = amazonIdentityManagement;
    }

    public DeleteRoleResult deleteRole(Role role) {

        List<DeleteRolePolicyRequest> inlinePolicies = listInlinePolicies(role);
        inlinePolicies.forEach(amazonIdentityManagement::deleteRolePolicy);
        return executeDeleteRoleRequest(role);
    }

    private List<DeleteRolePolicyRequest> listInlinePolicies(Role role) {
        try {
            return amazonIdentityManagement
                .listRolePolicies(new ListRolePoliciesRequest()
                    .withRoleName(role.getRoleName()))
                .getPolicyNames()
                .stream()
                .map(policyName -> new DeleteRolePolicyRequest()
                    .withRoleName(role.getRoleName())
                    .withPolicyName(policyName)).collect(Collectors.toList());
        } catch (NoSuchEntityException e) {
            logger.warn(NON_EXISTING_ROLE_POLICY_WARNING_MESSAGE, role.getRoleName());
            return Collections.emptyList();
        }
    }

    private DeleteRoleResult executeDeleteRoleRequest(Role role) {
        String roleName = role.getRoleName();
        try {
            return amazonIdentityManagement.deleteRole(
                new DeleteRoleRequest()
                    .withRoleName(roleName));
        } catch (NoSuchEntityException e) {
            logger.warn(NON_EXISTING_ROLE_WARNING_MESSAGE, roleName);
            return new DeleteRoleResult();
        }
    }
}
