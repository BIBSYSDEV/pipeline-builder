package no.bibsys.handler.templates;

import java.io.IOException;
import no.bibsys.handler.requests.ApiDocumentationInfo;
import no.bibsys.handler.responses.SimpleResponse;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;

public abstract class SwaggerHubEditor extends CodePipelineFunctionHandlerTemplate<SimpleResponse> {


    private final transient Environment environment = new Environment();

    protected transient String repository;
    protected transient String branch;
    protected transient String swaggerOrganization;
    protected transient String swaggerHubApiKey;
    protected transient String apiId;
    protected transient String apiVersion;
    protected transient String stage;
    protected transient String owner;

    protected void initFields() throws IOException {
        this.apiId = environment.readEnvOpt("API_ID").orElseThrow(() -> newException("API_ID"));
        this.apiVersion = environment.readEnvOpt("API_VERSION")
            .orElseThrow(() -> newException("API_VERSION"));
        this.owner = environment.readEnvOpt("OWNER").orElseThrow(() -> newException("OWNER"));
        this.branch = environment.readEnvOpt("BRANCH").orElseThrow(() -> newException("BRANCH"));
        this.repository = environment.readEnvOpt("REPOSITORY")
            .orElseThrow(() -> newException("REPOSITORY"));
        this.stage = environment.readEnvOpt("STAGE").orElseThrow(() -> newException("STAGE"));
        this.swaggerOrganization = environment.readEnvOpt("SWAGGER_ORG")
            .orElseThrow(() -> newException("SWAGGER_ORG"));

        SecretsReader secretsReader = new SecretsReader();
        this.swaggerHubApiKey = secretsReader.readAuthFromSecrets("swaggerapikey", "swaggerapikey");

    }


    protected ApiDocumentationInfo newPublishApi() {
        ApiDocumentationInfo publishApi = new ApiDocumentationInfo();
        publishApi.setApiId(apiId);
        publishApi.setApiVersion(apiVersion);
        publishApi.setStage(stage);
        publishApi.setSwaggerOrganization(swaggerOrganization);
        publishApi.setSwaggetHubApiKey(swaggerHubApiKey);
        publishApi.setBranch(branch);
        publishApi.setOwner(owner);
        publishApi.setRepository(repository);
        return publishApi;
    }



    private IllegalStateException newException(String missingVariable) {
        return new IllegalStateException(String.format("%s is missing\n", missingVariable));

    }





}
