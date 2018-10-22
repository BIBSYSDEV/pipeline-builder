package no.bibsys.roles;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.amazonaws.services.identitymanagement.model.Role;
import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.cloudformation.PipelineConfiguration;
import no.bibsys.utils.IoUtils;
import no.bibsys.utils.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RoleManagerTest {


    private  RoleManager roleManager;
    private IoUtils ioUtils=new IoUtils();

    @Before
    public void init() throws IOException {
        String assumePolicy= JsonUtils.removeComments(ioUtils.fileAsString(Paths.get("lambdaTrustRoleConfig","assumePolicy.json")));
        String accessPolicy=JsonUtils.removeComments(ioUtils.fileAsString(Paths.get("lambdaTrustRoleConfig","lambdaTrustRolePolicy.json")));

        PipelineConfiguration config=Mockito.mock(PipelineConfiguration.class);
        when(config.getLambdaTrustRolename()).thenReturn("MockRole");
        when(config.getLambdaTrustRoleAssumePolicy()).thenReturn(assumePolicy);
        when(config.getLambdaTrustRoleAccessPolicy()).thenReturn(accessPolicy);

        this.roleManager=new RoleManager(config);

    }

    @Test
    public void createRoleTest(){

        Role role=roleManager.createRole();
        assertThat(role,is(not(equalTo(null))));

        roleManager.deleteRole();
    }


}
