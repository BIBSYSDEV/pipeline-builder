package no.bibsys.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.GetExportRequest;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.RestApi;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.bibsys.cloudformation.CloudFormationConfigurable;

public class ApiExporter {


    private final transient CloudFormationConfigurable config;
    private final transient String stage;

    public ApiExporter(CloudFormationConfigurable config, String stage) {
        this.config = config;
        this.stage = stage;
    }


    public Optional<String> generateOpenApiNoExtensions() throws IOException {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("accepts", "application/json");
        return generateOpenApi(requestParameters);
    }

    private Optional<String> generateOpenApi(Map<String, String> requestParameters)
        throws IOException {

        AmazonApiGateway apiGateway = AmazonApiGatewayClientBuilder.defaultClient();
        List<RestApi> apiList = apiGateway
            .getRestApis(new GetRestApisRequest().withLimit(100)).getItems();

        Optional<RestApi> apiOpt = apiList.stream()
            .filter(api -> api.getName().contains(config.getProjectId()))
            .filter(api -> api.getName().contains(config.getNormalizedBranchName()))
            .filter(api -> api.getName().contains(stage))
            .findFirst();

        if (apiOpt.isPresent()) {
            RestApi api = apiOpt.get();

            GetExportRequest request = new GetExportRequest().withRestApiId(api.getId())
                .withStageName(stage).withExportType("oas30").withParameters(requestParameters);
            GetExportResult result = apiGateway
                .getExport(request);
            String swaggerFile = new String(result.getBody().array());
            return Optional.ofNullable(swaggerFile);

        }

        return Optional.empty();


    }


}
