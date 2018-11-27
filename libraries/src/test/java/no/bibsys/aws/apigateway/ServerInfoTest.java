package no.bibsys.aws.apigateway;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.Test;

public class ServerInfoTest {


    @Test
    public void completeServerUrl_HttpsserverUrlAndStage_workingSeverUrl() {
        ServerInfo serverInfo =
                new ServerInfo("https://xxxxxxxx.execute-api.eu-west-1.amazonaws.com/{basePath}", "final");
        assertThat(serverInfo.serverAddress(), is(equalTo("xxxxxxxx.execute-api.eu-west-1.amazonaws.com")));
    }

    @Test
    public void completeServerUrl_HttpserverUrlAndStage_workingSeverUrl() {
        ServerInfo serverInfo =
                new ServerInfo("http://xxxxxxxx.execute-api.eu-west-1.amazonaws.com/{basePath}", "final");
        assertThat(serverInfo.serverAddress(), is(equalTo("xxxxxxxx.execute-api.eu-west-1.amazonaws.com")));
    }

}
