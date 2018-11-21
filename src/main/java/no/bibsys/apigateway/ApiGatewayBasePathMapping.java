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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.bibsys.cloudformation.Stage;

public class ApiGatewayBasePathMapping {


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


    public CreateBasePathMappingResult createBasePath(RestApi restApi,String certifcateArn) {
        checkAndCreateCustomDomainName(certifcateArn);

        deleteBasePathMappings();

        CreateBasePathMappingRequest createBasePathMappingRequest = newBasePathMappingRequest(
            restApi.getId());

        return apiGatewayClient.createBasePathMapping(createBasePathMappingRequest);


    }


    public void deleteBasePathMappings() {
        System.out.println("Deleting old basepath Mappings");
        List<DeleteBasePathMappingRequest> deleteRequests = executeDeleteRequests();
        executeDeleteRequests(deleteRequests);
    }


    private void checkAndCreateCustomDomainName(String certifcateArn) {
        if (!domainExists(this.apiGatewayClient)) {
            createDomainName(this.apiGatewayClient,certifcateArn);
        }
    }


    public String getTargeDomainName() {
        String targetname = apiGatewayClient
            .getDomainName(new GetDomainNameRequest().withDomainName(domainName))
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
        try{
            Optional<List<BasePathMapping>> items = Optional
                .ofNullable(apiGatewayClient.getBasePathMappings(listBasePathsRequest).getItems());
            List<DeleteBasePathMappingRequest> result = items
                .map(list -> list.stream().map(item -> newDeleteBasePathRequest(item))
                    .collect(Collectors.toList())).orElse(Collections.emptyList());
            return result;
        }
        catch(NotFoundException e){
            e.printStackTrace();
            return Collections.emptyList();
        }



    }

    private DeleteBasePathMappingRequest newDeleteBasePathRequest(BasePathMapping item) {
        return new DeleteBasePathMappingRequest().withBasePath(item.getBasePath())
            .withDomainName(domainName);
    }


    private void createDomainName(AmazonApiGateway client, String certificateArn) {
        CreateDomainNameRequest createDomainNameRequest =
            new CreateDomainNameRequest().withRegionalCertificateArn(certificateArn)
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
