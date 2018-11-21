package no.bibsys.apigateway;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest;
import no.bibsys.cloudformation.Stage;
import org.junit.Test;

public class ApiGatewayBasePathMappingTest {

    private final String domainName = "domain.name.";

    @Test
    public void newBasePathMappingRequest_restApiIdTestStage_CreateBasePathMappingRequestWithRestApi() {

        ApiGatewayBasePathMapping apiGatewayBasePathMapping = new ApiGatewayBasePathMapping(null,
            domainName,
            Stage.TEST);

        CreateBasePathMappingRequest request = apiGatewayBasePathMapping
            .newBasePathMappingRequest("restApi");

        assertThat(request.getRestApiId(), is(equalTo("restApi")));
        assertThat(request.getDomainName(), is(equalTo("test." + domainName)));
        assertThat(request.getStage(), is(equalTo(Stage.TEST.toString())));
    }


    @Test
    public void newBasePathMappingRequest_restApiIdFinalStage_CreateBasePathMappingRequestWithRestApi() {
        String domainName = "domain.name.";
        ApiGatewayBasePathMapping apiGatewayBasePathMapping = new ApiGatewayBasePathMapping(null,
            domainName,
            Stage.FINAL);

        CreateBasePathMappingRequest request = apiGatewayBasePathMapping
            .newBasePathMappingRequest("restApi");

        assertThat(request.getRestApiId(), is(equalTo("restApi")));
        assertThat(request.getDomainName(), is(equalTo(domainName)));
        assertThat(request.getStage(), is(equalTo(Stage.FINAL.toString())));
    }

}
