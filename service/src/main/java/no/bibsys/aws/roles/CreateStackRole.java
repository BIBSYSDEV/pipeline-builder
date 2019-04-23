package no.bibsys.aws.roles;

import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleResult;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import java.io.IOException;
import no.bibsys.aws.utils.github.NotFoundException;
import no.bibsys.aws.utils.github.UnauthorizedException;

public interface CreateStackRole {

    CreateRoleRequest createNewCreateRoleRequest() throws IOException;

    PutRolePolicyRequest createNewPutRolePolicyRequest()
        throws IOException, UnauthorizedException, NotFoundException;

    AttachRolePolicyRequest createNewAttachPolicyRequest(String policyArn, String roleName);

    String createRole() throws IOException, UnauthorizedException, NotFoundException, Exception;

    DeleteRoleResult deleteRole();

    class PolicyAndRoleName {

        public final String roleName;
        public final String policyName;

        public PolicyAndRoleName(String roleName, String policyName) {
            this.roleName = roleName;
            this.policyName = policyName;
        }
    }
}
