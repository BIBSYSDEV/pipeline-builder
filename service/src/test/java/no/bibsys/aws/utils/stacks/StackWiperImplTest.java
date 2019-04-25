package no.bibsys.aws.utils.stacks;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyResult;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleResult;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult;
import com.amazonaws.services.identitymanagement.model.ListRoleTagsRequest;
import com.amazonaws.services.identitymanagement.model.ListRoleTagsResult;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.identitymanagement.model.Tag;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.s3.AmazonS3;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.roles.CreateStackRole;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;

public class StackWiperImplTest extends LocalStackTest {

    public static final int EXPECTED_NUMBER_OF_DELETED_ROLES = 1;
    public static final int INVOCATION_ARGUMENT = 0;
    private static final String[] SOME_POLICIES = {"policy1", "policy2"};
    private static final String SOME_TAG_KEY = "someKey";
    private static final String SOME_TAG_VALUE = "someTagValue";
    private static final String SOME_ROLE_TAG = "someRoleTag";
    private static final String SOME_BRANCH = "someBranch";
    private static final String SOME_PROJECT = "someProject";
    private static final int LIST_ROLES_LIST_ROLE_TAGS_LIST_ROLE_POLICIES_DELETE_ROLE_POLICIES_DELETE_ROLE = 5;
    private static final int CALLED_LIST_POLICIES = 1;
    private static final int CALLED_LIST_ROLES = 0;
    private static final int DELETE_ROLE_POLICIES = 2;
    private static final int DELETE_ROLE = 3;
    private static final int CALLED_LIST_ROLE_TAGS = 4;

    private StackWiperImpl createStackWiper(AmazonIdentityManagement mockIdentityManagement) {

        AmazonCloudFormation acf = mockCloudFormationWithStack();
        AmazonS3 s3 = mockS3Client();
        AWSLambda lambda = mockLambdaClient();
        AWSLogs logsClient = mockLogsClient();

        return new StackWiperImpl(pipelineStackConfiguration, acf,
            s3, lambda, logsClient, mockIdentityManagement);
    }

    @Test
    public void roleHasCorrectTagsShouldReturnTrueIfRoleHasCorrectTags() {

        Role role = createWellFormedRole();
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole()));
        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(true)));
    }

    @Test
    public void roleHasCorrectTagsShouldReturnTrueIfRoleHasAdditionalToCorrectTags() {

        Tag roleTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_ROLE)
            .withValue(CreateStackRole.ROLE_TAG_FOR_CREATE_STACK_ROLE);
        Tag branchTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());
        Tag projectTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());

        Tag anotherTag = new Tag().withKey(SOME_TAG_KEY).withValue(SOME_TAG_VALUE);
        Role role = new Role().withTags(roleTag, branchTag, projectTag, anotherTag);
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration, role));
        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(true)));
    }

    @Test
    public void roleHasCorrectTagsShouldReturnFalseIfAtLeastOneTagIsMissing() {

        Tag branchTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());
        Tag projectTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());

        Role role = new Role().withTags(branchTag, projectTag);
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration, role));
        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(false)));
    }

    @Test
    public void roleHasCorrectTagsShouldReturnFalseForAnEmptyTagSet() {
        Role role = new Role();
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration, role));
        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(false)));
    }

    @Test
    public void roleHasCorrectTagsShouldReturnFalseForAWrongTagSet() {
        Tag anotherTag = new Tag().withKey(SOME_TAG_KEY).withValue(SOME_TAG_VALUE);

        Tag branchTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());
        Tag projectTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());

        Role role = new Role().withTags(anotherTag, branchTag, projectTag);

        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration, role));
        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(false)));
    }

    @Test
    public void roleHasCorrectTagsShouldReturnTrueIfTagValuesAreWrong() {

        Tag roleTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_ROLE)
            .withValue(SOME_ROLE_TAG);
        Tag branchTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(SOME_BRANCH);
        Tag projectTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(SOME_PROJECT);

        Role role = new Role().withTags(roleTag, branchTag, projectTag);
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration, role));

        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(false)));
    }

    @Test
    public void rolesForDeletionShouldReturnOnlyRolesWithTheCorrectTags() {
        AmazonCloudFormation acf = mockCloudFormationWithStack();
        AmazonS3 s3 = mockS3Client();
        AWSLambda lambda = mockLambdaClient();
        AWSLogs logsClient = mockLogsClient();

        AmazonIdentityManagement mockIam = mock(AmazonIdentityManagement.class);

        HashMap<String, Role> roles = createTestRoles();
        when(mockIam.listRoles()).thenReturn(new ListRolesResult().withRoles(roles.values()));

        when(mockIam.listRoleTags(any())).thenAnswer(invocation -> {
            ListRoleTagsRequest request = invocation.getArgument(INVOCATION_ARGUMENT);
            Role role = roles.get(request.getRoleName());
            return new ListRoleTagsResult().withTags(role.getTags());
        });

        StackWiperImpl stackWiper = new StackWiperImpl(pipelineStackConfiguration, acf,
            s3, lambda, logsClient, mockIam);
        Role excpectedRole = createWellFormedRole();
        Role filteredOutRole = createRoleMissingOneTag();
        assertThat(stackWiper.rolesForDeletion(), contains(excpectedRole));
        assertThat(stackWiper.rolesForDeletion(), not(contains(filteredOutRole)));
    }

    @Test
    public void deleteCreateStackRoleShouldMakeCallsToDeleteARoleAndItsPolicies() {

        AmazonIdentityManagement iam = mock(AmazonIdentityManagement.class);
        boolean[] calledApisList =
            new boolean[LIST_ROLES_LIST_ROLE_TAGS_LIST_ROLE_POLICIES_DELETE_ROLE_POLICIES_DELETE_ROLE];

        iam = setUpListRoles(iam, calledApisList);
        iam = setUpListRolePolicies(iam, calledApisList);
        iam = setUpDeleteRolePolicies(iam, calledApisList);
        iam = setUpDeleteRole(iam, calledApisList);
        iam = setUpListRoleTags(iam, calledApisList);
        StackWiper stackWiper = new StackWiperImpl(pipelineStackConfiguration,
            mockCloudFormationWithStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            iam);
        stackWiper.wipeStacks();

        assertThat(calledApisList[CALLED_LIST_ROLES], is(true));
        assertThat(calledApisList[CALLED_LIST_POLICIES], is(true));
        assertThat(calledApisList[DELETE_ROLE_POLICIES], is(true));
        assertThat(calledApisList[DELETE_ROLE], is(true));
        assertThat(calledApisList[CALLED_LIST_ROLE_TAGS], is(true));
    }

    @Test
    public void deleteCreateStackRoleShouldDeleteOnlyTheAppropriateRole() {
        AmazonIdentityManagement iam = mock(AmazonIdentityManagement.class);

        HashMap<String, Role> roles = createTestRoles();

        when(iam.listRoles()).thenReturn(new ListRolesResult().withRoles(roles.values()));

        when(iam.listRolePolicies(any())).thenReturn(new ListRolePoliciesResult().withPolicyNames(SOME_POLICIES));

        when(iam.listRoleTags(any())).thenAnswer(invocation -> {
            ListRoleTagsRequest request = invocation.getArgument(INVOCATION_ARGUMENT);
            Role role = roles.get(request.getRoleName());
            return new ListRoleTagsResult().withTags(role.getTags());
        });

        List<String> deletedRolenames = new ArrayList<>();
        when(iam.deleteRole(any())).thenAnswer(invocation -> {

            DeleteRoleRequest request = invocation.getArgument(INVOCATION_ARGUMENT);
            String roleName = request.getRoleName();
            deletedRolenames.add(roleName);
            return new DeleteRoleResult();
        });

        StackWiper stackWiper = new StackWiperImpl(pipelineStackConfiguration,
            mockCloudFormationWithStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            iam);
        stackWiper.wipeStacks();

        assertThat(deletedRolenames.size(), is(equalTo(EXPECTED_NUMBER_OF_DELETED_ROLES)));
        assertThat(deletedRolenames, contains(pipelineStackConfiguration.getCreateStackRoleName()));
        assertThat(deletedRolenames, not(contains(createRoleMissingOneTag().getRoleName())));
    }

    private HashMap<String, Role> createTestRoles() {
        HashMap<String, Role> roles = new HashMap<>();
        Role wellFormedRole = createWellFormedRole();
        Role illFormedRole = createRoleMissingOneTag();
        roles.put(wellFormedRole.getRoleName(), wellFormedRole);
        roles.put(illFormedRole.getRoleName(), illFormedRole);
        return roles;
    }

    @Test
    void deleteStacks_pipelineStackConfiguration_deleteStackResults() {
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration));
        List<DeleteStackResult> results = stackWiper
            .deleteStacks();
        List<DeleteStackResult> resultList = new ArrayList<>(results);
        assertThat(resultList.isEmpty(), is((equalTo(false))));
    }

    @Test
    void deleteBuckets_pipelineStackConfiguration_noException() {
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration));
        stackWiper.deleteBuckets();
    }

    @Test
    public void wipeStacks_stackDoesNotExist_noException() {

        StackWiperImpl stackWiper = new StackWiperImpl(pipelineStackConfiguration,
            mockCloudFormationwithNoStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole()));
        stackWiper.wipeStacks();
    }

    @Test
    public void wipeStacks_stackDoesNotExist_rolesAreDeleted() {

        AmazonIdentityManagement iam = mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole());
        AtomicBoolean deleteRoleIsCalled = new AtomicBoolean(false);

        when(iam.deleteRole(any())).thenAnswer(invocation -> {
            deleteRoleIsCalled.set(true);
            return new DeleteRoleResult();
        });

        StackWiperImpl stackWiper = new StackWiperImpl(pipelineStackConfiguration,
            mockCloudFormationwithNoStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            iam);

        stackWiper.wipeStacks();
        assertThat(deleteRoleIsCalled.get(), is(true));
    }

    @Test
    void wipeStacks_pipelineExists_noException() {
        StackWiperImpl stackWiper = createStackWiper(
            mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole()));
        stackWiper.wipeStacks();
    }

    private AmazonIdentityManagement setUpDeleteRole(AmazonIdentityManagement mockIam, boolean[] calledApisList) {
        when(mockIam.deleteRole(any())).thenAnswer(invocation -> {
            calledApisList[DELETE_ROLE] = true;
            return new DeleteRoleResult();
        });
        return mockIam;
    }

    private AmazonIdentityManagement setUpDeleteRolePolicies(AmazonIdentityManagement mockIam,
        boolean[] calledApisList) {
        when(mockIam.deleteRolePolicy(any())).thenAnswer(invocation -> {
            calledApisList[DELETE_ROLE_POLICIES] = true;
            return new DeleteRolePolicyResult();
        });
        return mockIam;
    }

    private AmazonIdentityManagement setUpListRoles(AmazonIdentityManagement mockIam, boolean[] calledApisList) {
        when(mockIam.listRoles()).thenAnswer(invocation -> {
            calledApisList[CALLED_LIST_ROLES] = true;
            return new ListRolesResult()
                .withRoles(createWellFormedRole());
        });
        return mockIam;
    }

    private AmazonIdentityManagement setUpListRolePolicies(AmazonIdentityManagement mockIam,
        boolean[] calledApisList) {
        when(mockIam.listRolePolicies(any())).thenAnswer(invocation -> {
            calledApisList[CALLED_LIST_POLICIES] = true;
                return new ListRolePoliciesResult().withPolicyNames(SOME_POLICIES);
            }
        );

        return mockIam;
    }

    private AmazonIdentityManagement setUpListRoleTags(AmazonIdentityManagement mockIam, boolean[] calledApisList) {
        when(mockIam.listRoleTags(any())).thenAnswer(invocation -> {
                calledApisList[CALLED_LIST_ROLE_TAGS] = true;
                return new ListRoleTagsResult().withTags(createWellFormedRole().getTags());
            }
        );

        return mockIam;
    }
}
