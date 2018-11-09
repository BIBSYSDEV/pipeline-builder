package no.bibsys.apigateway;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
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


    @Test
    @Category(IntegrationTest.class)
    public void apiExporterShouldGenerateAnOpenApi3Documentation() throws IOException {
        Repository repo = FileRepositoryBuilder.create(new File(".", ".git"));

        String branch=repo.getBranch();

        CloudFormationConfigurable conf=new CloudFormationConfigurable(
            "authority-registry-infrastructure",branch){};
        ApiExporter apiExporter=new ApiExporter(conf,"test");
        Optional<String> apiJsonOpt = apiExporter.generateOpenApiNoExtensions();

        assertThat(apiJsonOpt.isPresent(),is(equalTo(true)));

        String apiJson=apiJsonOpt.get();
        Optional<String> openApiVersion = openApiVersion(apiJson);

        assertThat(openApiVersion.isPresent(),is(equalTo(true)));
        assertThat(openApiVersion.get(),is(equalTo("3.0.1")));

    }


    private Optional<String> openApiVersion(String json) throws IOException {

        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(json);
        String openApi = root.get("openapi").asText();
        return Optional.ofNullable(openApi);

    }


}
