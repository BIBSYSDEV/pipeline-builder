package no.bibsys.aws.roles;

import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleResult;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import java.io.IOException;
import no.bibsys.aws.utils.github.NotFoundException;
import no.bibsys.aws.utils.github.UnauthorizedException;

public interface CreateStackRole {

    String ROLE_TAG_FOR_CREATE_STACK_ROLE = "CreateStackRole";

    CreateRoleRequest createNewCreateRoleRequest() throws IOException;

    PutRolePolicyRequest createNewPutRolePolicyRequest()
        throws IOException, UnauthorizedException, NotFoundException;

    AttachRolePolicyRequest createNewAttachPolicyRequest(String policyArn, String roleName);

    String createRole() throws Exception;

    DeleteRoleResult deleteRole();
}
