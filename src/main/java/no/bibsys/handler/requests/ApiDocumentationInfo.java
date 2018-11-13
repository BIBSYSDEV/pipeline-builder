package no.bibsys.handler.requests;

public  final class ApiDocumentationInfo extends RepositoryInfo {


    private String stage;
    private String swaggerOrganization;
    private String apiId;
    private String apiVersion;
    private String swaggetHubApiKey;


    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getSwaggetHubApiKey() {
        return swaggetHubApiKey;
    }

    public void setSwaggetHubApiKey(String swaggetHubApiKey) {
        this.swaggetHubApiKey = swaggetHubApiKey;
    }

    public String getSwaggerOrganization() {
        return swaggerOrganization;
    }

    public void setSwaggerOrganization(String swaggerOrganization) {
        this.swaggerOrganization = swaggerOrganization;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
