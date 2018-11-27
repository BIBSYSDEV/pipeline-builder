package no.bibsys.aws.swaggerhub;

import java.io.IOException;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

public class SwaggerHubInfo {


    /**
     * The API id in SwaggerHub.
     */
    private static String API_ID_ENV = "API_ID";

    /**
     * The version of the API in SwaggerHub. This is the version of the API we deploy (e.g. 1.0, 1.0a). Not the version
     * of the standard we use for the API specification (e.g. oas3);
     */

    private static String API_VERSION = "API_VERSION";

    /**
     * The account name or the organization name to which this API belongs.
     */
    private static String SWAGGER_ORG = "SWAGGER_ORG";


    private static String AWS_SECRET_NAME = "swaggerapikey";
    private static String AWS_SECRET_KEY = "swaggerapikey";

    private final transient String apiId;
    private final transient String apiVersion;
    private final transient String swaggerOrganization;


    /**
     * SwaggerHub constructor with the use of {@link Environment}.
     *
     * @param environment an instance of {@link Environment}.
     */
    public SwaggerHubInfo(Environment environment) {
        this.apiId = environment.readEnv(API_ID_ENV);
        this.apiVersion = environment.readEnv(API_VERSION);
        this.swaggerOrganization = environment.readEnv(SWAGGER_ORG);

    }


    /**
     *  SwaggerHub constructor without the use of {@link Environment}.
     * @param apiId The id of the api
     * @param apiVersion The version of the API documentation. Can be {@code null} if the intended action is for the
     *        whole API
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

        SecretsReader secretsReader = new SecretsReader(AWS_SECRET_NAME, AWS_SECRET_KEY);
        return secretsReader.readSecret();

    }
}
