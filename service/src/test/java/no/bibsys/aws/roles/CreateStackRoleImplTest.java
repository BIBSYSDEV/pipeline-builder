package no.bibsys.aws.roles;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.JsonUtils;
import no.bibsys.aws.utils.github.GithubReader;
import no.bibsys.aws.utils.github.NotFoundException;
import no.bibsys.aws.utils.github.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateStackRoleImplTest {

    private static final String STATEMENT = "Statement";
    private static final String SERVICE = "Service";
    private static final String PRINCIPAL = "Principal";
    private static final String ACTION = "Action";
    private static final String STS_ASSUME_ROLE = "sts:AssumeRole";
    private static final String CLOUDFORMATION_AMAZONAWS_COM = "cloudformation.amazonaws.com";
    private static final Path CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON =
            Paths.get("..", "createStackRolePolicyDocument.json");
    private static final String ANY_CREATE_STACK_ROLE_POLICY_DOCUMENT = "Owls doobie doobie doo, says Orestis";
    private static final String ANY_CREATE_STACK_ROLE_POLICY_NAME = "Some vague hope of meaninglessness";
    private static final String SOME_ARN = "SomeArn";
    private static final String SOME_ROLE_NAME = "someRoleName";
    public static final String ROLE_NAME = "roleName";

    GithubReader mockGithubReader = mock(GithubReader.class);
    PipelineStackConfiguration mockPipelineStackConfiguration = mock(PipelineStackConfiguration.class);
    AmazonIdentityManagement mockAmazonIdentityManagement = mock(AmazonIdentityManagement.class);

    @Test
    void createStackRoleImplConstructorExists() {
        new CreateStackRoleImpl(mockGithubReader, mockPipelineStackConfiguration, mockAmazonIdentityManagement);
    }

    @Test
    void createStackRoleRequestShouldReturnCreateRoleRequestWithAssumePolicy() throws IOException {
        CreateStackRole createStackRole = new CreateStackRoleImpl(
                mockGithubReader, mockPipelineStackConfiguration, mockAmazonIdentityManagement);
        CreateRoleRequest createRoleRequest = createStackRole.createNewCreateRoleRequest();
        String assumeRolePolicyDocument = createRoleRequest.getAssumeRolePolicyDocument();
        String service = getPolicyStatementPrincipalService(assumeRolePolicyDocument);
        String assumeRole = getAssumeRole(assumeRolePolicyDocument);

        assertThat(service, is(equalTo(CLOUDFORMATION_AMAZONAWS_COM)));
        assertThat(assumeRole, is(equalTo(STS_ASSUME_ROLE)));
    }

    @Test
    void projectShouldContainCreateStackRolePolicyFile() throws IOException {
        String createStackRolePolicyDocument = IoUtils.fileAsString(CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON);
        assertThat(createStackRolePolicyDocument, is(not(emptyString())));
    }

    @Test
    void createNewRolePolicyRequestShouldReturnPolicyRequestWithUsersPolicy()
            throws IOException, UnauthorizedException, NotFoundException {
        when(mockGithubReader.readFile(any())).thenReturn(ANY_CREATE_STACK_ROLE_POLICY_DOCUMENT);
        when(mockPipelineStackConfiguration.getCreateStackRolePolicyName())
                .thenReturn(ANY_CREATE_STACK_ROLE_POLICY_NAME);

        CreateStackRole createStackRole = new CreateStackRoleImpl(
                mockGithubReader, mockPipelineStackConfiguration, mockAmazonIdentityManagement);
        PutRolePolicyRequest putRolePolicyRequest = createStackRole.createNewPutRolePolicyRequest();
        String expectedPolicyDocument = ANY_CREATE_STACK_ROLE_POLICY_DOCUMENT;
        String policyDocument = putRolePolicyRequest.getPolicyDocument();
        String expectedPolicyName = ANY_CREATE_STACK_ROLE_POLICY_NAME;

        assertThat(policyDocument, is(equalTo(expectedPolicyDocument)));
        assertThat(putRolePolicyRequest.getPolicyName(), is(equalTo(expectedPolicyName)));
    }

    @Test
    void createNewAttachRolePolicyRequestShouldReturnRequestContainingPolicyArnAndRoleName() {
        CreateStackRole createStackRole
                = new CreateStackRoleImpl(mockGithubReader, mockPipelineStackConfiguration, mockAmazonIdentityManagement);
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

        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(githubConf);

        when(mockAmazonIdentityManagement.createRole(any())).thenAnswer((Answer<CreateRoleResult>) invocation -> {
            CreateRoleRequest request = invocation.getArgument(0);
            return new CreateRoleResult().withRole(new Role().withRoleName(request.getRoleName()));
        });

        when(mockAmazonIdentityManagement.putRolePolicy(any())).thenReturn(new PutRolePolicyResult());
        when(mockAmazonIdentityManagement.getRole(any()))
                .thenReturn(new GetRoleResult().withRole(new Role()
                        .withRoleName(pipelineStackConfiguration.getCreateStackRoleName())));


        CreateStackRole createStackRole
                = new CreateStackRoleImpl(mockGithubReader, pipelineStackConfiguration, mockAmazonIdentityManagement);

        String result = createStackRole.createRole();

        assertThat(result, is(equalTo(pipelineStackConfiguration.getCreateStackRoleName())));
    }

    @Test
    void deleteRoleShouldReturnNonEmptyResult() {

        GithubConf githubConf = mock(GithubConf.class);
        when(githubConf.getBranch()).thenReturn("master-branch");
        when(githubConf.getRepository()).thenReturn("solitary-branch");
        when(githubConf.getOwner()).thenReturn("capital-owner");

        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(githubConf);

        Map<String, String> buffer = new HashMap<>();

        when(mockAmazonIdentityManagement.deleteRole(any())).thenAnswer((Answer<DeleteRoleResult>) invocation -> {
            DeleteRoleRequest request = invocation.getArgument(0);
            buffer.put(ROLE_NAME, request.getRoleName());
            return new DeleteRoleResult();
        });

        when(mockAmazonIdentityManagement.deletePolicy(any())).thenReturn(new DeletePolicyResult());
        when(mockAmazonIdentityManagement.detachRolePolicy(any())).thenReturn(new DetachRolePolicyResult());
        when(mockAmazonIdentityManagement.listRolePolicies(any())).thenReturn(new ListRolePoliciesResult());

        CreateStackRole createStackRole
                = new CreateStackRoleImpl(mockGithubReader, pipelineStackConfiguration, mockAmazonIdentityManagement);

        DeleteRoleResult deleteRoleResult = createStackRole.deleteRole();

        assertThat(deleteRoleResult, is(not(equalTo(null))));
        assertThat(buffer.get(ROLE_NAME), is(equalTo(pipelineStackConfiguration.getCreateStackRoleName())));
    }

    private String getAssumeRole(String assumeRolePolicyDocument) throws IOException {
        JsonNode statementsArray = getAssumeRolePolicyStatement(assumeRolePolicyDocument);
        String assumeRole = statementsArray.get(0).get(ACTION).textValue();
        return assumeRole;
    }

    private String getPolicyStatementPrincipalService(String assumeRolePolicyDocument) throws IOException {
        JsonNode statementsArray = getAssumeRolePolicyStatement(assumeRolePolicyDocument);
        ObjectNode principleObject = (ObjectNode) statementsArray.get(0).get(PRINCIPAL);
        return principleObject.get(SERVICE).textValue();
    }

    private JsonNode getAssumeRolePolicyStatement(String assumeRolePolicyDocument) throws IOException {
        ObjectNode policyDocument = (ObjectNode) JsonUtils.newJsonParser().readTree(assumeRolePolicyDocument);
        return policyDocument.get(STATEMENT);
    }
}
