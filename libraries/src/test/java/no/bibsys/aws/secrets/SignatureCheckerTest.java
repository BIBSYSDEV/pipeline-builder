package no.bibsys.aws.secrets;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.aws.tools.IoUtils;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class SignatureCheckerTest {



    @Test
    public void verifySecurityToken_secretValueAndBody_sha1Signature() throws IOException {
        String requestBody = IoUtils.resourceAsString(Paths.get("github", "sha_test_githubEvent.json"));
        String header = IoUtils.resourceAsString(Paths.get("github", "sha_test_github_header.txt"));
        String secretKey = "SECRETKEY";

        String expectedSignature = header.replaceFirst("sha1=", "");

        SignatureChecker signatureChecker = new SignatureChecker(null, null);

        byte[] actualSignatureBytes = signatureChecker.calculateExpectedSignature(requestBody, secretKey);

        String actualSignature = Hex.encodeHexString(actualSignatureBytes);

        assertThat(actualSignature, is(equalTo(expectedSignature)));


    }


}
