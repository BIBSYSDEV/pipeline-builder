package no.bibsys.lambda.api.utils;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.lambda.api.utils.SignatureChecker;
import no.bibsys.utils.Environment;
import no.bibsys.utils.IoUtils;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class SignatureCheckerTest {




    @Test
    public void verifySecurityToken_secretValueAndBody_sha1Signature()
        throws IOException {
        String requestBody = IoUtils
            .resourceAsString(Paths.get("requests", "sha_test_githubEvent.json"));
        String header = IoUtils
            .resourceAsString(Paths.get("requests", "sha_test_github_header.txt"));
        String secretKey = "SECRETKEY";

        String expectedSignature = header.replaceFirst("sha1=", "");


        SignatureChecker signatureChecker = new SignatureChecker("secretName","SECRETKEY");
        byte[] actualSignatureBytes = signatureChecker
            .calculateExpectedSignature(requestBody, secretKey);
        String actualSignature = Hex.encodeHexString(actualSignatureBytes);

        assertThat(actualSignature, is(equalTo(expectedSignature)));


    }


}
