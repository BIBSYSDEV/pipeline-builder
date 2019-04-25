package no.bibsys.aws.utils.stacks;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyResult;
import com.amazonaws.services.identitymanagement.model.DeleteRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRoleResult;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesResult;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.identitymanagement.model.Tag;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.s3.AmazonS3;
import java.util.ArrayList;
import java.util.List;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.roles.CreateStackRole;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;

public class StackWiperImplTest extends LocalStackTest {

    public static final int EXPECTED_NUMBER_OF_DELETED_ROLES = 1;
    public static final int INVOCATION_ARGUMENT = 0;
    private static final String ILLFORMED_ROLE_NAME = "illformedRole";
    private static final String[] SOME_POLICIES = {"policy1", "policy2"};
    private static final String SOME_TAG_KEY = "someKey";
    private static final String SOME_TAG_VALUE = "someTagValue";
    private static final String SOME_ROLE_TAG = "someRoleTag";
    private static final String SOME_BRANCH = "someBranch";
    private static final String SOME_PROJECT = "someProject";
    private static final int LIST_ROLE_POLICIES = 1;
    private static final int LIST_ROLES_LIST_ROLE_POLICIES_DELETE_ROLE_POLICIES_DELETE_ROLE = 4;
    private static final int CALLED_LIST_POLICIES = 1;
    private static final int CALLED_LIST_ROLES = 0;
    private static final int DELETE_ROLE_POLICIES = 2;
    private static final int DELETE_ROLE = 3;
    private StackWiperImpl stackWiper;

    public StackWiperImplTest() {

        AmazonCloudFormation acf = mockCloudFormationWithStack();
        AmazonS3 s3 = mockS3Client();
        AWSLambda lambda = mockLambdaClient();
        AWSLogs logsClient = mockLogsClient();
        AmazonIdentityManagement mockIdentityManagement = mockIdentityManagement(pipelineStackConfiguration);

        this.stackWiper = new StackWiperImpl(pipelineStackConfiguration, acf,
            s3, lambda, logsClient, mockIdentityManagement);
    }

    @Test
    public void roleHasCorrectTagsShouldReturnTrueIfRoleHasCorrectTags() {

        Role role = createWellFormedRole();

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

        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(true)));
    }

    @Test
    public void roleHasCorrectTagsShouldReturnFalseIfAtLeastOneTagIsMissing() {

        Tag branchTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());
        Tag projectTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());

        Role role = new Role().withTags(branchTag, projectTag);

        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(false)));
    }

    @Test
    public void roleHasCorrectTagsShouldReturnFalseForAnEmptyTagSet() {
        Role role = new Role();
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

        assertThat(stackWiper.roleHasCorrectTags(role), is(equalTo(false)));
    }

    @Test
    public void rolesForDeletionShouldReturnOnlyRolesWithTheCorrectTags() {
        AmazonCloudFormation acf = mockCloudFormationWithStack();
        AmazonS3 s3 = mockS3Client();
        AWSLambda lambda = mockLambdaClient();
        AWSLogs logsClient = mockLogsClient();

        AmazonIdentityManagement mockIam = mock(AmazonIdentityManagement.class);

        Role expectedRole = createWellFormedRole();
        Role filteredOutRole = createIllformedRole();
        when(mockIam.listRoles()).thenReturn(new ListRolesResult().withRoles(expectedRole, filteredOutRole));

        StackWiperImpl stackWiper = new StackWiperImpl(pipelineStackConfiguration, acf,
            s3, lambda, logsClient, mockIam);
        assertThat(stackWiper.rolesForDeletion(), contains(expectedRole));
        assertThat(stackWiper.rolesForDeletion(), not(contains(filteredOutRole)));
    }

    @Test
    public void deleteCreateStackRoleShouldMakeCallsToDeleteARoleAndItsPolicies() {

        AmazonIdentityManagement iam = mock(AmazonIdentityManagement.class);
        boolean[] calledApisList = new boolean[LIST_ROLES_LIST_ROLE_POLICIES_DELETE_ROLE_POLICIES_DELETE_ROLE];

        iam = setUpListRoles(iam, calledApisList);
        iam = setUpListRolePolicies(iam, calledApisList);
        iam = setUpDeleteRolePolicies(iam, calledApisList);
        iam = setUpDeleteRole(iam, calledApisList);

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
    }

    @Test
    public void deleteCreateStackRoleShouldDeleteOnlyTheAppropriateRole() {
        AmazonIdentityManagement iam = mock(AmazonIdentityManagement.class);

        when(iam.listRoles()).thenReturn(new ListRolesResult().withRoles(createWellFormedRole(),
            createIllformedRole()));

        when(iam.listRolePolicies(any())).thenReturn(new ListRolePoliciesResult().withPolicyNames(SOME_POLICIES));

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
        assertThat(deletedRolenames, not(contains(createIllformedRole().getRoleName())));
    }

    @Test
    void deleteStacks_pipelineStackConfiguration_deleteStackResults() {
        List<DeleteStackResult> results = stackWiper
            .deleteStacks();
        List<DeleteStackResult> resultList = new ArrayList<>(results);
        assertThat(resultList.isEmpty(), is((equalTo(false))));
    }

    @Test
    void deleteBuckets_pipelineStackConfiguration_noException() {
        stackWiper.deleteBuckets();
    }

    @Test
    public void wipeStacks_stackDoesNotExist_exception() {
        StackWiperImpl stackWiper = new StackWiperImpl(pipelineStackConfiguration,
            mockCloudFormationwithNoStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            mockIdentityManagement(pipelineStackConfiguration));
        assertThrows(AmazonCloudFormationException.class, stackWiper::wipeStacks);
    }

    @Test
    void wipeStacks_pipelineExists_noException() {
        stackWiper.wipeStacks();
    }

    private Role createIllformedRole() {
        Tag roleTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_ROLE)
            .withValue(CreateStackRole.ROLE_TAG_FOR_CREATE_STACK_ROLE);
        Tag branchTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());

        return new Role().withTags(roleTag, branchTag).withRoleName(ILLFORMED_ROLE_NAME);
    }

    private Role createWellFormedRole() {
        Tag roleTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_ROLE)
            .withValue(CreateStackRole.ROLE_TAG_FOR_CREATE_STACK_ROLE);
        Tag branchTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());
        Tag projectTag = new Tag().withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());

        return new Role().withTags(roleTag, branchTag, projectTag)
            .withRoleName(pipelineStackConfiguration.getCreateStackRoleName());
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
        boolean[] calledListPolices) {
        when(mockIam.listRolePolicies(any())).thenAnswer(invocation -> {
                calledListPolices[CALLED_LIST_POLICIES] = true;
                return new ListRolePoliciesResult().withPolicyNames(SOME_POLICIES);
            }
        );

        return mockIam;
    }
}
