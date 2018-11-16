package no.bibsys.handler;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import no.bibsys.utils.Environment;
import no.bibsys.utils.IoUtils;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class GithubHandlerTest {

    Environment environment = Mockito.mock(Environment.class);

    @Before
    public void init() {
        when(environment.readEnvOpt(anyString())).thenAnswer((Answer<String>) invocation -> {
            String input = invocation.getArgument(0);
            return input;
        });
    }

    @Test
    public void verifySecurityToken_secretValueAndBody_sha1Signature()
        throws IOException {
        String requestBody = IoUtils
            .resourceAsString(Paths.get("requests", "sha_test_githubEvent.json"));
        String header = IoUtils
            .resourceAsString(Paths.get("requests", "sha_test_github_header.txt"));
        String secretKey = "SECRETKEY";

        String expectedSignature = header.replaceFirst("sha1=", "");

        SignatureChecker signatureChecker = new SignatureChecker(environment);
        byte[] actualSignatureBytes = signatureChecker.getActualSignature(requestBody, secretKey);
        String actualSignature = Hex.encodeHexString(actualSignatureBytes);

        assertThat(actualSignature, is(equalTo(expectedSignature)));


    }


}
