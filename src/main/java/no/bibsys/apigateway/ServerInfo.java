package no.bibsys.apigateway;

public class ServerInfo {

    private final String serverUrl;
    private final String stage;
    private final static String basePathString = "/{basePath}";

    public ServerInfo(String serverUrl, String stage) {
        this.serverUrl = serverUrl;

        if (serverUrl.contains(basePathString) && stage.charAt(0) == '/') {
            this.stage = stage.substring(1);
        } else {
            this.stage = stage;
        }
    }

    /**
     * It return  the Server URL in the form of  https://xxxxxxxx.execute-api.eu-west-1.amazonaws.com/{basePath}.
     * It is intended for to be used by Swagger and SwaggerHub.
     *
     * @return the Server url to use in the OpenApi specification.
     */
    public String getServerUrl() {
        return serverUrl;
    }


    /**
     * @return The ApiGateway Stage for the API. Swagger will use this value to replace {@code
     * {basePath}}
     */
    public String getStage() {
        return stage;
    }


    /**
     *
     * @return The Server URL where {@code {basepath}} has been replaced by {@code stage}.
     *
     */
    public String completeServerUrl() {
        return serverUrl.replace(basePathString, "/" + stage);
    }
}
