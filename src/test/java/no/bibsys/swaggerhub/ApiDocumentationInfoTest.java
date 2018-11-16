package no.bibsys.swaggerhub;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.swaggerhub.ApiDocumentationInfo;
import no.bibsys.utils.JsonUtils;
import org.junit.Test;

public class ApiDocumentationInfoTest {


    private String test = "{"
        + "\"stage\":\"test\","
        + "\"swaggetHubApiKey\":\"apiKey\","
        + "\"apiVersion\":\"apiVersion\","
        + "\"apiId\":\"api123\""
        + "}";


    @Test
    public void ApiDocumentationInfoBean_jsonOBject_apiDocumenationInfo() throws IOException {
        ObjectMapper parser= JsonUtils.newJsonParser();
        ApiDocumentationInfo phase=parser.readValue(test, ApiDocumentationInfo.class);
        assertThat(phase.getStage(),is(equalTo("test")));
        assertThat(phase.getSwaggetHubApiKey(),is(equalTo("apiKey")));
        assertThat(phase.getApiVersion(),is(equalTo("apiVersion")));
        assertThat(phase.getApiId(),is(equalTo("api123")));
    }



}
