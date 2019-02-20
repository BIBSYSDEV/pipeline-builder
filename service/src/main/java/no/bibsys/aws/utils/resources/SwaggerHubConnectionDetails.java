package no.bibsys.aws.utils.resources;

import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;

public class SwaggerHubConnectionDetails {

    private final transient SwaggerHubInfo swaggerHubInfo;
    private final transient SecretsReader swaggerHubSecretsReader;

    public SwaggerHubConnectionDetails(SwaggerHubInfo swaggerHubInfo, SecretsReader secretsReader) {
        this.swaggerHubInfo = swaggerHubInfo;
        this.swaggerHubSecretsReader = secretsReader;
    }

    public SwaggerHubInfo getSwaggerHubInfo() {
        return swaggerHubInfo;
    }

    public SecretsReader getSwaggerHubSecretsReader() {
        return swaggerHubSecretsReader;
    }
}
