package no.bibsys.aws.apigateway;

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
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.IntegrationTest;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

public class ApiGatewayApiInfoTest {


    private transient String apiJson;
    private transient JsonNode root;

    private AmazonApiGateway apiGateway = Mockito.mock(AmazonApiGateway.class);


    @Before

    public void init() throws IOException {
        Environment environment = new Environment();
        String stackName = environment.readEnv("STACK_NAME");
        apiJson = generateOpenApiSpec(stackName).orElse(null);
        root = parseOpenApiSpec(apiJson);
    }


    @Test
    @Category(IntegrationTest.class)
    public void generateOpenApiNoExtensions_existingAPIGatewayEndpoint_JsonString() {
        assertThat(apiJson, is(not(equalTo(null))));
        assertThat(apiJson.isEmpty(), is(equalTo(false)));
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
        String serverUrl = getServerUrl(root);
        assertThat(serverUrl, is(not(equalTo(null))));
        assertThat(serverUrl.isEmpty(), is(equalTo(false)));
    }


    @Test
    @Category(IntegrationTest.class)
    public void generateOpenApiNoExtensions_existingAPIGatewayEndpoint_validVBasePath() {
        String basePath = getBasePath(root);
        assertThat(basePath, is(not(equalTo(null))));
        assertThat(basePath.isEmpty(), is(equalTo(false)));
        assertThat(basePath, is(equalTo("test")));
    }

    private Optional<String> generateOpenApiSpec(String stackName) throws IOException {

        String restApiId = restApiId(stackName);
        ApiGatewayApiInfo apiGatewayApiInfo = new ApiGatewayApiInfo(Stage.TEST, apiGateway, restApiId);
        return apiGatewayApiInfo.generateOpenApiNoExtensions();
    }

    private String restApiId(String stackName) {

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
