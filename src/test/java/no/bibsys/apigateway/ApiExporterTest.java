package no.bibsys.apigateway;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.utils.IntegrationTest;
import no.bibsys.utils.JsonUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ApiExporterTest {


    private String apiJson;
    private JsonNode root;
    public ApiExporterTest() throws IOException {
        apiJson = generateOpenApiSpec().orElse(null);
        root = parseOpenApiSpec(apiJson);
    }


    @Test
    @Category(IntegrationTest.class)
    public void apiExporter_existingAPIGatewayEndpoint_JsonString() {
        assertThat(apiJson,is(not(equalTo(null))));
        assertThat(apiJson.isEmpty(),is(equalTo(false)));
    }

    @Test
    @Category(IntegrationTest.class)
    public void apiExporter_existingAPIGatewayEndpoint_OpenAPI3Version() throws IOException {

        Optional<String> openApiVersion = openApiVersion(root);
        assertThat(openApiVersion.isPresent(), is(equalTo(true)));
        assertThat(openApiVersion.get(), is(equalTo("3.0.1")));

    }


    @Test
    @Category(IntegrationTest.class)
    public void apiExporter_existingAPIGatewayEndpoint_ValidServerUrl() {
        String serverUrl=getServerUrl(root);
        assertThat(serverUrl,is(not(equalTo(null))));
        assertThat(serverUrl.isEmpty(),is(equalTo(false)));
    }


    @Test
    @Category(IntegrationTest.class)
    public void apiExporter_existingAPIGatewayEndpoint_validVBasePath() {
        String basePath=getBasePath(root);
        assertThat(basePath,is(not(equalTo(null))));
        assertThat(basePath.isEmpty(),is(equalTo(false)));
        assertThat(basePath,is(equalTo("test")));
    }

    private Optional<String> generateOpenApiSpec() throws IOException {
        CloudFormationConfigurable conf = buildConfiguration();
        ApiExporter apiExporter = new ApiExporter(conf, "test");
        return apiExporter.generateOpenApiNoExtensions();
    }

    private CloudFormationConfigurable buildConfiguration() throws IOException {
        Repository repo = FileRepositoryBuilder.create(new File(".", ".git"));
        String branch = repo.getBranch();

        return new CloudFormationConfigurable(
            "authority-registry-infrastructure", branch) {
        };
    }


    private Optional<String> openApiVersion(JsonNode root) throws IOException {
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
