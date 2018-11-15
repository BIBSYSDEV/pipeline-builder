package no.bibsys.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import no.bibsys.utils.IoUtils;
import org.junit.Test;

public class GithubHandlerTest {


    @Test
    public void verifySecurityToken_secretValueAndBody_sha1Signature()
        throws IOException, NoSuchMethodException {
        String requestBody = IoUtils
            .resourceAsString(Paths.get("requests", "sha_test_githubEvent.json"));
        String header = IoUtils
            .resourceAsString(Paths.get("requests", "sha_test_github_header.txt"));
        String secretKey = "0";

        GithubHandler githubHandler = new GithubHandler();
        githubHandler.validateSignature(header, requestBody, null, secretKey);


    }


}
