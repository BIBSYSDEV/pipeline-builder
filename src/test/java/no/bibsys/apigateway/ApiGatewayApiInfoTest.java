package no.bibsys.apigateway;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.kms.model.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.cloudformation.PipelineConfiguration;
import no.bibsys.cloudformation.helpers.ResourceType;
import no.bibsys.cloudformation.helpers.StackResources;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubConf;
import no.bibsys.utils.Environment;
import no.bibsys.utils.IntegrationTest;
import no.bibsys.utils.JsonUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

public class ApiGatewayApiInfoTest {


    private String apiJson;
    private JsonNode root;

    private AmazonApiGateway apiGateway= Mockito.mock(AmazonApiGateway.class);
    public ApiGatewayApiInfoTest() throws IOException {
        apiJson = generateOpenApiSpec().orElse(null);
        root = parseOpenApiSpec(apiJson);
    }


    @Test
    @Category(IntegrationTest.class)
    public void generateOpenApiNoExtensions_existingAPIGatewayEndpoint_JsonString() {
        assertThat(apiJson,is(not(equalTo(null))));
        assertThat(apiJson.isEmpty(),is(equalTo(false)));
    }

    @Test
    @Category(IntegrationTest.class)
    public void generateOpenApiNoExtensions_existingAPIGatewayEndpoint_OpenAPI3Version() {

        Optional<String> openApiVersion = openApiVersion(root);
        assertThat(openApiVersion.isPresent(), is(equalTo(true)));
        assertThat(openApiVersion.get(), is(equalTo("3.0.1")));

    }


    @Test
    @Category(IntegrationTest.class)
    public void generateOpenApiNoExtensionsexistingAPIGatewayEndpoint_ValidServerUrl() {
        String serverUrl=getServerUrl(root);
        assertThat(serverUrl,is(not(equalTo(null))));
        assertThat(serverUrl.isEmpty(),is(equalTo(false)));
    }


    @Test
    @Category(IntegrationTest.class)
    public void generateOpenApiNoExtensions_existingAPIGatewayEndpoint_validVBasePath() {
        String basePath=getBasePath(root);
        assertThat(basePath,is(not(equalTo(null))));
        assertThat(basePath.isEmpty(),is(equalTo(false)));
        assertThat(basePath,is(equalTo("test")));
    }

    private Optional<String> generateOpenApiSpec() throws IOException {

        String restApiId = restApiId();
        ApiGatewayApiInfo apiGatewayApiInfo = new ApiGatewayApiInfo("test", apiGateway, restApiId);
        return apiGatewayApiInfo.generateOpenApiNoExtensions();
    }

    private String restApiId() {
        GitInfo gitIfo = new GithubConf(new Environment());
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration(
            gitIfo.getRepository(), gitIfo.getBranch());
        String stackName = pipelineConfiguration.getTestServiceStack();
        StackResources stackResources = new StackResources(stackName);

        String result = stackResources.getResourceIds(ResourceType.REST_API).stream().findAny()
            .orElseThrow(() -> new NotFoundException("RestApi not Found for stack:" + stackName));
        return result;
    }


    private Optional<String> openApiVersion(JsonNode root) {
        String openApi = root.get("openapi").asText();
        return Optional.ofNullable(openApi);

    }

    private JsonNode parseOpenApiSpec(String json) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        return mapper.readTree(json);
    }


    private String getServerUrl(JsonNode root) {
        JsonNode serversNode = root.get("servers").get(0);
        return serversNode.get("url").asText();
    }


    private String getBasePath(JsonNode root) {
        JsonNode serversNode = root.get("servers").get(0);
        return serversNode.get("variables").get("basePath").get("default").asText();

    }


}
