package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BasePathMapping;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingResult;
import com.amazonaws.services.apigateway.model.CreateDomainNameRequest;
import com.amazonaws.services.apigateway.model.DeleteBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.DeleteDomainNameRequest;
import com.amazonaws.services.apigateway.model.EndpointConfiguration;
import com.amazonaws.services.apigateway.model.EndpointType;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsRequest;
import com.amazonaws.services.apigateway.model.GetDomainNameRequest;
import com.amazonaws.services.apigateway.model.NotFoundException;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.bibsys.aws.cloudformation.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiGatewayBasePathMapping {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayBasePathMapping.class);
    private final transient AmazonApiGateway apiGatewayClient;
    private final transient String domainName;
    private final transient Stage stage;


    public ApiGatewayBasePathMapping(AmazonApiGateway apiGatewayClient,
        String domainName,
        Stage stage) {
        this.apiGatewayClient = apiGatewayClient;
        this.stage = stage;
        this.domainName = domainName;

    }


    public CreateBasePathMappingResult createBasePath(String apiGatewayRestApiId,
        String certifcateArn) {
        deleteBasePathMappings();
        checkAndCreateCustomDomainName(certifcateArn);

        CreateBasePathMappingRequest createBasePathMappingRequest = newBasePathMappingRequest(
            apiGatewayRestApiId);

        return apiGatewayClient.createBasePathMapping(createBasePathMappingRequest);
    }


    public void deleteBasePathMappings() {
        System.out.println("Deleting old basepath Mappings");

        try {
            List<DeleteBasePathMappingRequest> deleteRequests = createDeleteRequests();
            executeDeleteRequests(deleteRequests);
            DeleteDomainNameRequest deleteDomainNameRequest = new DeleteDomainNameRequest()
                .withDomainName(domainName);
            apiGatewayClient.deleteDomainName(deleteDomainNameRequest);
        } catch (NotFoundException e) {
            log.warn("Custom domain name not found");
        }

    }


    private void checkAndCreateCustomDomainName(String certifcateArn) {
        if (!domainExists(this.apiGatewayClient)) {
            createDomainName(this.apiGatewayClient, certifcateArn);
        }
    }


    public Optional<String> getTargetDomainName() throws NotFoundException {
        try {
            String targetname = apiGatewayClient
                .getDomainName(new GetDomainNameRequest().withDomainName(domainName))
                .getRegionalDomainName();
            return Optional.ofNullable(targetname);
        } catch (NotFoundException e) {
            return Optional.empty();
        }


    }


    @VisibleForTesting
    public CreateBasePathMappingRequest newBasePathMappingRequest(String restApiId) {
        return new CreateBasePathMappingRequest().withRestApiId(restApiId)
            .withDomainName(domainName)
            .withStage(stage.toString());
    }

    private void executeDeleteRequests(List<DeleteBasePathMappingRequest> deleteRequests) {
        deleteRequests.forEach(apiGatewayClient::deleteBasePathMapping);
    }

    private List<DeleteBasePathMappingRequest> createDeleteRequests() {
        GetBasePathMappingsRequest listBasePathsRequest = new GetBasePathMappingsRequest()
            .withDomainName(domainName);
        try {
            Optional<List<BasePathMapping>> items = Optional
                .ofNullable(apiGatewayClient.getBasePathMappings(listBasePathsRequest).getItems());
            List<DeleteBasePathMappingRequest> result = items
                .map(list -> list.stream().map(this::newDeleteBasePathRequest)
                    .collect(Collectors.toList())).orElse(Collections.emptyList());
            return result;
        } catch (NotFoundException e) {
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
