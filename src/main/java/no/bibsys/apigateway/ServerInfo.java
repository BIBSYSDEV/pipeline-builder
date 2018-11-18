package no.bibsys.apigateway;

public class ServerInfo {

    private final String serverUrl;
    private final String stage;


    public ServerInfo(String serverUrl, String stage) {
        this.serverUrl = serverUrl;

        if (serverUrl.contains("/{basePath}") && stage.charAt(0) == '/') {
            this.stage = stage.substring(1);
        } else {
            this.stage = stage;
        }
    }


    public String getServerUrl() {
        return serverUrl;
    }

    public String getStage() {
        return stage;
    }
}
