package no.bibsys.aws.utils.stacks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Role;
import java.io.IOException;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.testtutils.LocalStackTest;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class StackBuilderTest extends LocalStackTest {

    public static final String MOCKED_ERROR_MESSAGE = "A Role with name %s does not exist";
    public static final String NON_EXISTING_ROLENAME = "notExistingRolename";
    private final transient AmazonIdentityManagement mockAmazonIdentityManagement;
    private final transient AmazonCloudFormation cloudFormationWithStack;
    private final transient StackWiper wiper;

    public StackBuilderTest() {
        cloudFormationWithStack = mockCloudFormationWithStack();

        mockAmazonIdentityManagement = mockIdentityManagement(pipelineStackConfiguration);
        when(mockAmazonIdentityManagement.getRole(any()))
            .thenAnswer((Answer<GetRoleResult>) this::mockGetRoleExecution);
        wiper = new StackWiperImpl(
            pipelineStackConfiguration,
            mockCloudFormationwithNoStack(),
            mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(),
            mockAmazonIdentityManagement
        );
    }

    private GetRoleResult mockGetRoleExecution(InvocationOnMock invocation) {
        GetRoleRequest roleRequest = invocation.getArgument(0);
        String roleName = roleRequest.getRoleName();
        if (pipelineStackConfiguration.getCreateStackRoleName().equals(roleName)) {
            return new GetRoleResult()
                .withRole(new Role()
                    .withArn(pipelineStackConfiguration.getCreateStackRoleName()));
        } else {
            throw new NoSuchEntityException(String.format(MOCKED_ERROR_MESSAGE, roleName));
        }
    }

    @Test
    public void getCreateStackRoleArn_notExistingRole_NoSuchEntityException() throws IOException {
        PipelineStackConfiguration pipelineStackConfiguration = spy(this.pipelineStackConfiguration);
        when(pipelineStackConfiguration.getCreateStackRoleName()).thenReturn(NON_EXISTING_ROLENAME);

        StackBuilder stackBuilder = new StackBuilder(wiper,
            pipelineStackConfiguration,
            cloudFormationWithStack,
            mockAmazonIdentityManagement,
            mockGithubReader().setGitHubConf(mockGithubConf())
        );
        assertThrows(NoSuchEntityException.class, stackBuilder::getCreateStackRoleArn);
    }

    @Test
    public void getCreateStackRoleArn_existingRole_RoleArn() throws IOException {
        StackBuilder stackBuilder = new StackBuilder(wiper,
            pipelineStackConfiguration,
            cloudFormationWithStack,
            mockAmazonIdentityManagement,
            mockGithubReader().setGitHubConf(mockGithubConf())
        );
        assertThat(stackBuilder.getCreateStackRoleArn(),
            is(equalTo(pipelineStackConfiguration.getCreateStackRoleName())));
    }

    @Test
    public void createStacks_notExistingStack_noException() throws Exception {
        AmazonIdentityManagement mockIam = mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole());
        StackWiper wiper = new StackWiperImpl(pipelineStackConfiguration, cloudFormationWithStack, mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(), mockIam);
        StackBuilder stackBuilder = new StackBuilder(
            wiper,
            pipelineStackConfiguration,
            cloudFormationWithStack,
            mockIam,
            mockGithubReader().setGitHubConf(mockGithubConf()));


        stackBuilder.createStacks();
    }

    @Test
    public void createStacks_existingStack_noException() throws Exception {
        AmazonIdentityManagement mockIam = mockIdentityManagement(pipelineStackConfiguration, createWellFormedRole());
        StackWiper wiper = new StackWiperImpl(pipelineStackConfiguration, cloudFormationWithStack, mockS3Client(),
            mockLambdaClient(),
            mockLogsClient(), mockIam);
        StackBuilder stackBuilder = new StackBuilder(
            wiper, pipelineStackConfiguration, cloudFormationWithStack,
            mockIam,
            mockGithubReader().setGitHubConf(mockGithubConf()));

        stackBuilder.createStacks();
    }
}
