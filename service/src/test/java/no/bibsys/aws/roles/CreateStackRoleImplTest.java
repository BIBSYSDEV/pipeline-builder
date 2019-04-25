package no.bibsys.aws.roles;

import static no.bibsys.aws.roles.CreateStackRoleImpl.MISSING_CONFIGURATION_EXCEPTION_MESSAGE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyResult;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.identitymanagement.model.Tag;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.testtutils.LocalStackTest;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.JsonUtils;
import no.bibsys.aws.utils.github.GithubReader;
import no.bibsys.aws.utils.github.NotFoundException;
import no.bibsys.aws.utils.github.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class CreateStackRoleImplTest extends LocalStackTest {

    private static final String STATEMENT = "Statement";
    private static final String SERVICE = "Service";
    private static final String PRINCIPAL = "Principal";
    private static final String ACTION = "Action";
    private static final String STS_ASSUME_ROLE = "sts:AssumeRole";
    private static final String CLOUDFORMATION_AMAZONAWS_COM = "cloudformation.amazonaws.com";
    private static final Path CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON =
        Paths.get("..", "createStackRolePolicyDocument.json");
    private static final String ANY_CREATE_STACK_ROLE_POLICY_DOCUMENT = "Owls doobie doobie doo, says Orestis";
    private static final String SOME_ARN = "SomeArn";
    private static final String SOME_ROLE_NAME = "someRoleName";

    private transient GithubReader mockGithubReader;
    private transient AmazonIdentityManagement mockAmazonIdentityManagement;

    @BeforeEach
    public void init() {
        mockGithubReader = mock(GithubReader.class);
        mockAmazonIdentityManagement = mock(AmazonIdentityManagement.class);
    }

    @Test
    void createStackRoleImplConstructorExists() {
        new CreateStackRoleImpl(mockGithubReader, pipelineStackConfiguration,
            mockAmazonIdentityManagement);
    }

    @Test
    void createStackRoleRequestShouldReturnCreateRoleRequestWithAssumePolicy() throws IOException {
        CreateStackRole createStackRole = new CreateStackRoleImpl(
            mockGithubReader, pipelineStackConfiguration, mockAmazonIdentityManagement);
        CreateRoleRequest createRoleRequest = createStackRole.createNewCreateRoleRequest();
        String assumeRolePolicyDocument = createRoleRequest.getAssumeRolePolicyDocument();
        String service = getPolicyStatementPrincipalService(assumeRolePolicyDocument);
        String assumeRole = getAssumeRole(assumeRolePolicyDocument);

        assertThat(service, is(equalTo(CLOUDFORMATION_AMAZONAWS_COM)));
        assertThat(assumeRole, is(equalTo(STS_ASSUME_ROLE)));
    }

    @Test
    void createStackRoleRequestShouldReturnCreateRoleRequestWithExpectedTags() throws IOException {
        CreateStackRole createStackRole = new CreateStackRoleImpl(
            mockGithubReader, pipelineStackConfiguration, mockAmazonIdentityManagement);
        CreateRoleRequest createRoleRequest = createStackRole.createNewCreateRoleRequest();

        List<Tag> actualTags = createRoleRequest.getTags();

        Tag projectIdTag = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());

        Tag branchTag = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());

        Tag roleTag = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_ROLE)
            .withValue(CreateStackRole.ROLE_TAG_FOR_CREATE_STACK_ROLE);

        assertThat(actualTags, containsInAnyOrder(projectIdTag, branchTag, roleTag));
    }

    @Test
    void projectShouldContainCreateStackRolePolicyFile() throws IOException {
        String createStackRolePolicyDocument = IoUtils
            .fileAsString(CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON);
        assertThat(createStackRolePolicyDocument, is(not(emptyString())));
    }

    @Test
    void createNewRolePolicyRequestShouldReturnPolicyRequestWithUsersPolicy()
        throws IOException, UnauthorizedException, NotFoundException, MissingConfigurationException {
        when(mockGithubReader.readFile(any())).thenReturn(ANY_CREATE_STACK_ROLE_POLICY_DOCUMENT);

        CreateStackRole createStackRole = new CreateStackRoleImpl(
            mockGithubReader, pipelineStackConfiguration, mockAmazonIdentityManagement);
        PutRolePolicyRequest putRolePolicyRequest = createStackRole.createNewPutRolePolicyRequest();
        String expectedPolicyDocument = ANY_CREATE_STACK_ROLE_POLICY_DOCUMENT;
        String policyDocument = putRolePolicyRequest.getPolicyDocument();
        String expectedPolicyName = pipelineStackConfiguration.getCreateStackRolePolicyName();

        assertThat(policyDocument, is(equalTo(expectedPolicyDocument)));
        assertThat(putRolePolicyRequest.getPolicyName(), is(equalTo(expectedPolicyName)));
    }

    @Test
    void createNewAttachRolePolicyRequestShouldReturnRequestContainingPolicyArnAndRoleName() {
        CreateStackRole createStackRole
            = new CreateStackRoleImpl(mockGithubReader, pipelineStackConfiguration,
            mockAmazonIdentityManagement);
        AttachRolePolicyRequest attachRolePolicyRequest
            = createStackRole.createNewAttachPolicyRequest(SOME_ARN, SOME_ROLE_NAME);
        String expectedArn = SOME_ARN;
        String expectedRoleName = SOME_ROLE_NAME;
        assertThat(attachRolePolicyRequest.getPolicyArn(), is(equalTo(expectedArn)));
        assertThat(attachRolePolicyRequest.getRoleName(), is(equalTo(expectedRoleName)));
    }

    @Test
    void createRoleShouldReturnNonEmptyResult() throws Exception {
        GithubConf githubConf = mock(GithubConf.class);
        when(githubConf.getBranch()).thenReturn("master-branch");
        when(githubConf.getRepository()).thenReturn("solitary-branch");
        when(githubConf.getOwner()).thenReturn("capital-owner");

        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(
            githubConf);

        when(mockAmazonIdentityManagement.createRole(any()))
            .thenAnswer((Answer<CreateRoleResult>) invocation -> {
                CreateRoleRequest request = invocation.getArgument(0);
                return new CreateRoleResult()
                    .withRole(new Role().withRoleName(request.getRoleName()));
            });

        when(mockAmazonIdentityManagement.putRolePolicy(any()))
            .thenReturn(new PutRolePolicyResult());
        when(mockAmazonIdentityManagement.getRole(any()))
            .thenReturn(new GetRoleResult().withRole(new Role()
                .withRoleName(pipelineStackConfiguration.getCreateStackRoleName())));

        CreateStackRole createStackRole
            = new CreateStackRoleImpl(mockGithubReader, pipelineStackConfiguration,
            mockAmazonIdentityManagement);

        String result = createStackRole.createRole();

        assertThat(result, is(equalTo(pipelineStackConfiguration.getCreateStackRoleName())));
    }

    @Test
    public void createNewPutRolePolicyRequestShouldThrowExceptionForMissingConfigurationFile() throws IOException {
        GithubReader githubReader = new GithubReader(mockHttpClientReturningNotFound()).setGitHubConf(mockGithubConf());
        CreateStackRole createStackRole = new CreateStackRoleImpl(githubReader, pipelineStackConfiguration,
            mockAmazonIdentityManagement);
        MissingConfigurationException exception = assertThrows(MissingConfigurationException.class,
            createStackRole::createNewPutRolePolicyRequest);

        assertThat(exception.getMessage(), containsString(MISSING_CONFIGURATION_EXCEPTION_MESSAGE));
    }

    private String getAssumeRole(String assumeRolePolicyDocument) throws IOException {
        JsonNode statementsArray = getAssumeRolePolicyStatement(assumeRolePolicyDocument);
        String assumeRole = statementsArray.get(0).get(ACTION).textValue();
        return assumeRole;
    }

    private String getPolicyStatementPrincipalService(String assumeRolePolicyDocument)
        throws IOException {
        JsonNode statementsArray = getAssumeRolePolicyStatement(assumeRolePolicyDocument);
        ObjectNode principleObject = (ObjectNode) statementsArray.get(0).get(PRINCIPAL);
        return principleObject.get(SERVICE).textValue();
    }

    private JsonNode getAssumeRolePolicyStatement(String assumeRolePolicyDocument)
        throws IOException {
        ObjectNode policyDocument = (ObjectNode) JsonUtils.newJsonParser()
            .readTree(assumeRolePolicyDocument);
        return policyDocument.get(STATEMENT);
    }
}

