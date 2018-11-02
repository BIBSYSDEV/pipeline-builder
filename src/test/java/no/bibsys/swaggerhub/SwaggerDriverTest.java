package no.bibsys.swaggerhub;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import no.bibsys.utils.IntegrationTest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class SwaggerDriverTest {


    private final transient String organization="unit";
    private final transient String apiId="api-id";
    private final transient String apiKey="ApIKeY";
    private final transient String apiVersion="2.1";
    private final transient SwaggerDriver driver=new SwaggerDriver(apiKey,organization,apiId);


    @Test
    public void UpdateRequestURLPathShouldIncludeOnlyOrganizationAndApiId()
        throws URISyntaxException, MalformedURLException {
        HttpPost post = postRequest();
        String path = post.getURI().toURL().getPath();
        assertThat(path,(containsString(organization)));
        assertThat(path,(containsString(apiId)));

    }

    @Test
    public void UpdateRequestURLPathShouldNotApiVersion()
        throws URISyntaxException, MalformedURLException {
        String apiVersion="2.1";
        HttpPost post = postRequest();
        String path = post.getURI().toURL().getPath();
        assertThat(path,not(containsString(apiVersion)));
    }


    @Test
    public void UpdateRequestURLParametersShouldIncludeApiVersion()
        throws URISyntaxException {

        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters,containsString("version"));
        assertThat(parameters,containsString(apiVersion));
    }



    @Test
    public void UpdateRequestURLParametersShouldIncludeForceParamater()
        throws URISyntaxException {

        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters,containsString("force"));
    }


    @Test
    public void UpdateRequestURLParametersShouldIncludeIsPrivateParamater()
        throws URISyntaxException {

        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters,containsString("isPrivate"));
    }



    @Test
    public void DeleteRequestURLPathShouldIncludeOrganizationApiIdAndVersion()
        throws URISyntaxException, MalformedURLException {
        HttpDelete delete=deleteRequest();
        assertThat(delete.getURI().toURL().toString(),containsString(organization));
        assertThat(delete.getURI().toURL().toString(),containsString(apiId));
        assertThat(delete.getURI().toURL().toString(),containsString(apiVersion));
        assertThat(delete.getURI().toURL().getQuery(),is(equalTo(null)));
    }



    @Test
    @Category(IntegrationTest.class)

    private HttpDelete deleteRequest() throws URISyntaxException {
        return driver.createDeleteSpecificationVesionRequest(apiVersion);
    }


    private HttpPost postRequest() throws URISyntaxException {
        return driver
            .createUpdateSpecificationPostRequest("jsonString", apiVersion);
    }


}
