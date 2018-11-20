package no.bibsys.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.GetExportRequest;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.RestApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.utils.IoUtils;
import no.bibsys.utils.JsonUtils;

public class ApiGatewayApiInfo {


    private final transient CloudFormationConfigurable config;
    private final transient String stage;
    private final transient AmazonApiGateway client;

    public ApiGatewayApiInfo(CloudFormationConfigurable config, String stage,AmazonApiGateway apiGatewayClient) {
        this.config = config;
        this.stage = stage;
        Preconditions.checkNotNull(stage);
        this.client=apiGatewayClient;
    }







    public Optional<String> generateOpenApiNoExtensions() throws IOException {
        String openApiTemplate = readOpenApiTemplate();
        Optional<ServerInfo> serverInfo = readServerInfo();
        Optional<String> updatedOpenApiSpecification = serverInfo
            .map(server -> injectServerInfo(openApiTemplate, server));

        if(updatedOpenApiSpecification.isPresent()){
            String openApiJsonSpec = JsonUtils.yamlToJson(updatedOpenApiSpecification.get());
            return  Optional.of(openApiJsonSpec);
        }
        return Optional.empty();


    }

    public Optional<ServerInfo> readServerInfo() throws IOException {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("accepts", "application/json");
        Optional<JsonNode> amazonApiSpec = readOpenApiSpecFromAmazon(requestParameters);
        return amazonApiSpec.map(apiSpec -> generateServerInfo(apiSpec));


    }



    private String injectServerInfo(String openApiTemplate, ServerInfo serverInfo) {
        return openApiTemplate.replace("<SERVER_PLACEHOLDER>",serverInfo.getServerUrl())
            .replace("<STAGE_PLACEHOLDER>",serverInfo.getStage());
    }




    private Optional<JsonNode> readOpenApiSpecFromAmazon(Map<String, String> requestParameters)
        throws IOException {

        Optional<RestApi> apiOpt = findRestApi();

        if (apiOpt.isPresent()) {
            RestApi api = apiOpt.get();

            GetExportRequest request = new GetExportRequest().withRestApiId(api.getId())
                .withStageName(stage).withExportType(ApiGatewayConstants.OPEN_API_3)
                .withParameters(requestParameters);
            GetExportResult result = client
                .getExport(request);
            String swaggerFile = new String(result.getBody().array());
            ObjectMapper parser = JsonUtils.newJsonParser();
            return Optional.ofNullable(parser.readTree(swaggerFile));

        }

        return Optional.empty();


    }


    private ServerInfo generateServerInfo(JsonNode openApiSpec) {
        JsonNode serversNode=openApiSpec.get("servers").get(0);
        String serverUrl = serversNode.get("url").asText();
        String stage = serversNode.get("variables").get("basePath").get("default").asText();
        return new ServerInfo(serverUrl, stage);

    }

    public Optional<RestApi> findRestApi() {
        List<RestApi> apiList = client
            .getRestApis(new GetRestApisRequest().withLimit(100)).getItems();

        return apiList.stream()
            .filter(api -> api.getName().contains(config.getProjectId()))
            .filter(api -> api.getName().contains(config.getNormalizedBranchName()))
            .filter(api -> api.getName().contains(stage))
            .findFirst();
    }


    private String readOpenApiTemplate() throws IOException {
        return IoUtils.resourceAsString(Paths.get("openapi", "openapi.yml"));


    }





}
