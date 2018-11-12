package no.bibsys.handler.requests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.bibsys.utils.JsonUtils;
import org.junit.Test;

public class ApiDocumentationInfoTest {


    private String test="{\"owner\":\"OWNER\","
        + "\"repository\":\"REPOSITORY\","
        + "\"branch\":\"testBranch\","
        + "\"stage\":\"test\","
        + "\"swaggetHubApiKey\":\"apiKey\","
        + "\"apiVersion\":\"apiVersion\","
        + "\"apiId\":\"api123\""
        + "}";


    @Test
    public void BuildPhaseTestShouldBeInitlializedThroughAJsonObjet() throws IOException {
        ObjectMapper parser= JsonUtils.newJsonParser();
        ApiDocumentationInfo phase=parser.readValue(test, ApiDocumentationInfo.class);
        assertThat(phase.getStage(),is(equalTo("test")));
        assertThat(phase.getOwner(),is(equalTo("OWNER")));
        assertThat(phase.getRepository(),is(equalTo("REPOSITORY")));
        assertThat(phase.getBranch(),is(equalTo("testBranch")));
        assertThat(phase.getSwaggetHubApiKey(),is(equalTo("apiKey")));
        assertThat(phase.getApiVersion(),is(equalTo("apiVersion")));
        assertThat(phase.getApiId(),is(equalTo("api123")));
    }



}
