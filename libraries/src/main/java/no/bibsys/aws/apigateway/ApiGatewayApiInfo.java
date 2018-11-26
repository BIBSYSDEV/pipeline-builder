package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.GetExportRequest;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.amazonaws.services.apigateway.model.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.JsonUtils;


public class ApiGatewayApiInfo {



    private final transient String stage;
    private final transient AmazonApiGateway client;
    private final transient String restApiId;

    public ApiGatewayApiInfo(String stage, AmazonApiGateway apiGatewayClient, String restApiId) {

        this.stage = stage;
        Preconditions.checkNotNull(stage);
        this.client = apiGatewayClient;
        this.restApiId = restApiId;
    }


    public Optional<String> generateOpenApiNoExtensions() throws IOException {
        String openApiTemplate = readOpenApiTemplate();
        Optional<ServerInfo> serverInfo = readServerInfo();
        if (serverInfo.isPresent()) {
            ServerInfo info = serverInfo.get();
            String updatedOpenApiSpecification = injectServerInfo(openApiTemplate, info);
            String openApiJsonSpec = JsonUtils.yamlToJson(updatedOpenApiSpecification);
            return Optional.of(openApiJsonSpec);

        }
        return Optional.empty();


    }

    public Optional<ServerInfo> readServerInfo() throws IOException {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("accepts", "application/json");
        Optional<JsonNode> amazonApiSpec = readOpenApiSpecFromAmazon(requestParameters);
        return amazonApiSpec.map(apiSpec -> generateServerInfo(apiSpec));


    }


    /**
     * We desire a richer OpenApi documentation than the one that Amazon currently provides. So we
     * read the server address from Api Gateway and we inject the information to the custom OpenApi
     * specification.
     *
     * @param openApiTemplate Custom OpenApi specification
     * @param serverInfo Server URL and URL path variables  as produced by ApiGateway
     * @return A Swagger documentation with the correct Server URL
     */
    private String injectServerInfo(String openApiTemplate, ServerInfo serverInfo)
        throws IOException {

        String replacedSever = openApiTemplate
            .replace("<SERVER_PLACEHOLDER>", serverInfo.getServerUrl());
        if (serverInfo.getStage() != null) {
            return replacedSever.replace("<STAGE_PLACEHOLDER>", serverInfo.getStage());
        } else {
            ObjectMapper yamlParser = JsonUtils.newYamlParser();
            ObjectNode root = (ObjectNode) yamlParser.readTree(replacedSever);
            ArrayNode servers=(ArrayNode) root.get("servers");
            ObjectNode server=(ObjectNode)servers.get(0);
            server.remove("variables");
            String removedVariables = yamlParser.writeValueAsString(root);
            return removedVariables;


        }

    }


    private Optional<JsonNode> readOpenApiSpecFromAmazon(Map<String, String> requestParameters)
        throws IOException {

        try{
            GetExportRequest request = new GetExportRequest().withRestApiId(restApiId)
                .withStageName(stage).withExportType(ApiGatewayConstants.OPEN_API_3)
                .withParameters(requestParameters);
            GetExportResult result = client
                .getExport(request);
            String swaggerFile = new String(result.getBody().array());
            ObjectMapper parser = JsonUtils.newJsonParser();
            return Optional.ofNullable(parser.readTree(swaggerFile));

        }
        catch(NotFoundException e) {
            return Optional.empty();
        }

    }


    private ServerInfo generateServerInfo(JsonNode openApiSpec) {
        JsonNode serversNode = openApiSpec.get("servers").get(0);
        String serverUrl = serversNode.get("url").asText();
        String apiStage = getStageVariable(serversNode).orElse(null);
        return new ServerInfo(serverUrl, apiStage);

    }

    private Optional<String> getStageVariable(JsonNode serversNode) {
        Optional<String> apiStage = Optional.ofNullable(serversNode.get("variables"))
            .map(var -> var.get("basePath"))
            .map(basePath -> basePath.get("default"))
            .map(deflt -> deflt.asText());
        return apiStage;
    }





    private String readOpenApiTemplate() throws IOException {
        return IoUtils.resourceAsString(Paths.get("openapi", "openapi.yml"));


    }


}
