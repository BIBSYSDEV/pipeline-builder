package no.bibsys.lambda.deploy.handlers;

import java.io.IOException;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;

public class SwaggerHubInfo {


    private final transient String apiId;
    private final transient String apiVersion;
    private final transient String swaggerOrganization;


    public SwaggerHubInfo(Environment environment) {
        this.apiId = environment.readEnvOpt("API_ID").orElse("simple-api");
        this.apiVersion = environment.readEnvOpt("API_VERSION").orElse("1.0");
        this.swaggerOrganization = environment.readEnvOpt("SWAGGER_ORG").orElse("axthosarouris");

    }


    /**
     * @param apiId The id of the api
     * @param apiVersion The version of the API documentation. Can be {@code null} if the intended
     * action is for the whole API
     * @param swaggerOrganization The SwaggerHub organization or account name
     */


    public SwaggerHubInfo(String apiId, String apiVersion, String swaggerOrganization) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.swaggerOrganization = swaggerOrganization;
    }


    public String getApiId() {
        return apiId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getSwaggerOrganization() {
        return swaggerOrganization;
    }

    public String getSwaggerAuth() throws IOException {

        SecretsReader secretsReader = new SecretsReader();
        return secretsReader.readAuthFromSecrets("swaggerapikey", "swaggerapikey");

    }
}
