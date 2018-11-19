package no.bibsys.apigateway;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

public class ServerInfoTest {


    private ServerInfo serverInfo = new ServerInfo(
        "https://xxxxxxxx.execute-api.eu-west-1.amazonaws.com/{basePath}", "final");


    @Test
    public void completeServerUrl_serverUrlAndStage_workingSeverUrl() {
        assertThat(serverInfo.completeServerUrl(),
            is(equalTo("https://xxxxxxxx.execute-api.eu-west-1.amazonaws.com/final")));
    }

}
