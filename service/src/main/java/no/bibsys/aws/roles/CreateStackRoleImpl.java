package no.bibsys.aws.roles;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.identitymanagement.model.Tag;
import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.utils.github.GithubReader;
import no.bibsys.aws.utils.github.NotFoundException;
import no.bibsys.aws.utils.github.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateStackRoleImpl implements CreateStackRole {

    protected static final String MISSING_CONFIGURATION_EXCEPTION_MESSAGE =
        "Missing configuration exception. Probably due to missing role policy document file.";
    private static final Logger logger = LoggerFactory.getLogger(CreateStackRoleImpl.class);
    private static final String TEMPLATES_DIRECTORY = "templates";
    private static final String CREATE_STACK_ROLE_ASSUME_POLICY_JSON = "createStackRoleAssumePolicy.json";
    private static final String CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON = "createStackRolePolicyDocument.json";
    private static final String CREATE_STACK_ROLE_DESCRIPTION = "Role that allows creation of resources in a deployed"
        + " service";
    private static final String CREATE_ROLE_SUCCESS_MESSAGE = "Created role with roleName {}";
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
        logger.info(CREATE_ROLE_SUCCESS_MESSAGE, role.getRoleName());
        return role.getRoleName();
    }

    @Override
    public CreateRoleRequest createNewCreateRoleRequest() throws IOException {
        String assumeRolePolicy = IoUtils.resourceAsString(
            Paths.get(TEMPLATES_DIRECTORY, CREATE_STACK_ROLE_ASSUME_POLICY_JSON));
        Tag projectIdTag = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_PROJECT_ID)
            .withValue(pipelineStackConfiguration.getProjectId());
        Tag branch = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_BRANCH_NAME)
            .withValue(pipelineStackConfiguration.getPipelineConfiguration().getBranchName());
        Tag role = new Tag()
            .withKey(PipelineStackConfiguration.TAG_KEY_ROLE)
            .withValue(ROLE_TAG_FOR_CREATE_STACK_ROLE);

        return new CreateRoleRequest()
            .withAssumeRolePolicyDocument(assumeRolePolicy)
            .withRoleName(this.pipelineStackConfiguration.getCreateStackRoleName())
            .withDescription(CREATE_STACK_ROLE_DESCRIPTION)
            .withTags(projectIdTag, branch, role);
    }

    @Override
    public PutRolePolicyRequest createNewPutRolePolicyRequest()
        throws IOException, UnauthorizedException, MissingConfigurationException {
        try {
            String createStackRolePolicyDocument = githubReader
                .readFile(Paths.get(CREATE_STACK_ROLE_POLICY_DOCUMENT_JSON));
            return new PutRolePolicyRequest()
                .withPolicyDocument(createStackRolePolicyDocument)
                .withPolicyName(pipelineStackConfiguration.getCreateStackRolePolicyName())
                .withRoleName(pipelineStackConfiguration.getCreateStackRoleName());
        } catch (NotFoundException e) {
            throw new MissingConfigurationException(
                MISSING_CONFIGURATION_EXCEPTION_MESSAGE, e);
        }
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
