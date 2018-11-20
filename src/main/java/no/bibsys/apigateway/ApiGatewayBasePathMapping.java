package no.bibsys.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BasePathMapping;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingResult;
import com.amazonaws.services.apigateway.model.CreateDomainNameRequest;
import com.amazonaws.services.apigateway.model.DeleteBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.EndpointConfiguration;
import com.amazonaws.services.apigateway.model.EndpointType;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsRequest;
import com.amazonaws.services.apigateway.model.GetDomainNameRequest;
import com.amazonaws.services.apigateway.model.NotFoundException;
import com.amazonaws.services.apigateway.model.RestApi;
import java.util.List;
import java.util.stream.Collectors;
import no.bibsys.cloudformation.Stage;

public class ApiGatewayBasePathMapping {

    private final static String CERTIFICATE_NAME = "global-entitydata-certificate";
    private final transient AmazonApiGateway apiGatewayClient;
    private final transient String domainName;
    private final transient Stage stage;




    public ApiGatewayBasePathMapping(AmazonApiGateway apiGatewayClient,
        String domainName,
        Stage stage) {
        this.apiGatewayClient = apiGatewayClient;
        this.domainName = domainName;
        this.stage = stage;
    }




    public CreateBasePathMappingResult createBasePath(RestApi restApi) {
        checkAndCreateCustomDomainName();

        List<DeleteBasePathMappingRequest> deleteRequests = executeDeleteRequests();
        executeDeleteRequests(deleteRequests);

        CreateBasePathMappingRequest createBasePathMappingRequest = newBasePathMappingRequest(
            restApi.getId());

        return apiGatewayClient.createBasePathMapping(createBasePathMappingRequest);


    }


    private void checkAndCreateCustomDomainName() {
        if (!domainExists(this.apiGatewayClient)) {
            createDomainName(this.apiGatewayClient);
        }
    }




    public String getTargeDomainName(){
        String targetname= apiGatewayClient.getDomainName(new GetDomainNameRequest().withDomainName(domainName))
            .getRegionalDomainName();
        return targetname;
    }


    private CreateBasePathMappingRequest newBasePathMappingRequest(String restApiId) {
        return new CreateBasePathMappingRequest().withRestApiId(restApiId)
            .withDomainName(domainName)
            .withStage(stage.toString());
    }

    private void executeDeleteRequests(List<DeleteBasePathMappingRequest> deleteRequests) {
        deleteRequests.stream().forEach(request -> apiGatewayClient.deleteBasePathMapping(request));
    }

    private List<DeleteBasePathMappingRequest> executeDeleteRequests() {
        GetBasePathMappingsRequest listBasePathsRequest = new GetBasePathMappingsRequest()
            .withDomainName(domainName);

        return apiGatewayClient.getBasePathMappings(listBasePathsRequest).getItems().stream().map(
            item -> newDeleteBasePathRequest(item)).collect(Collectors.toList());

    }

    private DeleteBasePathMappingRequest newDeleteBasePathRequest(BasePathMapping item) {
        return new DeleteBasePathMappingRequest().withBasePath(item.getBasePath())
            .withDomainName(domainName);
    }


    private void createDomainName(AmazonApiGateway client) {
        CreateDomainNameRequest createDomainNameRequest =
            new CreateDomainNameRequest().withRegionalCertificateName(CERTIFICATE_NAME)
                .withDomainName(domainName)
                .withEndpointConfiguration(new EndpointConfiguration().withTypes(
                    EndpointType.REGIONAL));

        client.createDomainName(createDomainNameRequest);
    }

    private boolean domainExists(AmazonApiGateway client) {
        try {
             client.getDomainName(new GetDomainNameRequest().withDomainName(domainName));
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }


}
