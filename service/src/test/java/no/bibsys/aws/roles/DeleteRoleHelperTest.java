package no.bibsys.aws.roles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.DeleteRoleResult;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Role;
import java.util.Collections;
import no.bibsys.aws.testtutils.LocalStackTest;
import no.bibsys.aws.utils.github.GithubReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteRoleHelperTest extends LocalStackTest {

    private static final String NON_EXISTING_ROLE_ERROR_MESSAGE = "Role does not exist";
    private static final String ROLE_NAME = "roleName";
    private transient GithubReader mockGithubReader;
    private transient AmazonIdentityManagement mockAmazonIdentityManagement;

    @BeforeEach
    public void init() {
        mockGithubReader = mock(GithubReader.class);
        mockAmazonIdentityManagement = mock(AmazonIdentityManagement.class);
        when(mockAmazonIdentityManagement.listRolePolicies(any()))
            .thenReturn(new ListRolePoliciesResult().withPolicyNames(Collections.emptyList()));

        when(mockAmazonIdentityManagement.deleteRole(any()))
            .thenReturn(new DeleteRoleResult());
    }

    @Test
    void deleteRoleShouldReturnNonEmptyResult() {
        DeleteRoleHelper deleteRoleHelper = new DeleteRoleHelper(mockAmazonIdentityManagement);
        Role role = new Role().withRoleName(ROLE_NAME);
        DeleteRoleResult result = deleteRoleHelper.deleteRole(role);
        assertThat(result, is(not(equalTo(null))));
    }

    @Test
    public void deleteRoleShouldNotFailForNonExistingRole() {
        when(mockAmazonIdentityManagement.listRolePolicies(any()))
            .thenReturn(new ListRolePoliciesResult().withPolicyNames("somePolicy"));
        when(mockAmazonIdentityManagement.deleteRole(any())).thenThrow(new NoSuchEntityException(
            NON_EXISTING_ROLE_ERROR_MESSAGE));
        DeleteRoleHelper deleteRoleHelper = new DeleteRoleHelper(mockAmazonIdentityManagement);
        Role role = new Role().withRoleName(ROLE_NAME);
        assertThat(deleteRoleHelper.deleteRole(role), is(not(equalTo(null))));
    }

    @Test
    public void deleteRoleShouldNotFailForNonExistingRolePolicies() {
        when(mockAmazonIdentityManagement.listRolePolicies(any()))
            .thenThrow(new NoSuchEntityException(NON_EXISTING_ROLE_ERROR_MESSAGE));
        when(mockAmazonIdentityManagement.deleteRole(any())).thenThrow(new NoSuchEntityException(
            NON_EXISTING_ROLE_ERROR_MESSAGE));
        DeleteRoleHelper deleteRoleHelper = new DeleteRoleHelper(mockAmazonIdentityManagement);
        Role role = new Role().withRoleName(ROLE_NAME);
        assertThat(deleteRoleHelper.deleteRole(role), is(not(equalTo(null))));
    }
}
