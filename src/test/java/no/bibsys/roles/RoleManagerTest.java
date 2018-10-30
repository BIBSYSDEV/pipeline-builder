package no.bibsys.roles;


import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import no.bibsys.cloudformation.PipelineConfiguration;
import no.bibsys.utils.AmazonDependentTest;
import no.bibsys.utils.IoUtils;
import no.bibsys.utils.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class RoleManagerTest {


    private RoleManager roleManager;
    private String branch = "the-branch";
    private String repositoryName = "a-repository";
    private String assumePolicy;
    private String accessPolicy;

    @Before
    public void init() throws IOException {
        assumePolicy = JsonUtils.removeComments(IoUtils
            .fileAsString(Paths.get("lambdaTrustRoleConfig", "assumePolicy.json")));
        this.accessPolicy = JsonUtils.removeComments(IoUtils
            .fileAsString(Paths.get("lambdaTrustRoleConfig", "lambdaTrustRolePolicy.json")));
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration(repositoryName,
            branch,
            assumePolicy,
            accessPolicy
        );

        this.roleManager = new RoleManager(pipelineConfiguration);
    }


    @Test
    @Category(AmazonDependentTest.class)
    public void roleManagerShouldUpdateARole() throws IOException {

        if(roleManager.getRole().isPresent()){
            roleManager.deleteRole();
        }

         roleManager.createRole();

        String accessPolicy = JsonUtils.removeComments(IoUtils
            .resourceAsString(Paths.get("mockRolePolicies", "lambdaTrustRolePolicy.json")));

        PipelineConfiguration newPipelineConfig = new PipelineConfiguration(repositoryName,
            branch,
            assumePolicy,
            accessPolicy
        );

        roleManager=new RoleManager(newPipelineConfig);
        roleManager.updateRole();

        List<String> roleAccessPolicy= roleManager.listInlinePolicies();


        assertThat(roleAccessPolicy.size(),is(equalTo(1)));

        assertThat(compareJsons(accessPolicy,roleAccessPolicy.get(0)),is(equalTo(true)));

        roleManager.deleteRole();

    }


    private boolean compareJsons(String json1,String json2) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode node1=mapper.readTree(json1);
        JsonNode node2=mapper.readTree(json2);
        return node1.equals(node2);


    }


}
