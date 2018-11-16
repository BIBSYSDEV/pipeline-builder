package no.bibsys.lambda.api.utils;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SignatureChecker {

    private static final String SIGNATURE_PREFIX = "sha1=";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final Logger logger = LogManager.getLogger(SignatureChecker.class);

    public static String SECRET_NAME = "SECRET_NAME";
    public static String SECRET_KEY = "SECRET_KEY";
    private transient String amazonSecretName;
    private transient String amazonSecretKey;

    private final transient Environment environment;


    public SignatureChecker(Environment environment) {
        this.environment = environment;
    }


    public boolean verifySecurityToken(String token, String requestBody) throws IOException {
        String privateKey = readSecretsKey();
        return validateSignature(token, requestBody, privateKey);
    }


    private boolean validateSignature(
        String signatureHeader,
        String body,
        String webhookSecret
    ) {

        if (webhookSecret == null || webhookSecret.equals("")) {
            logger.debug(
                "{}.webhookSecret not configured. Skip signature validation");
            return true;
        }

        byte[] signature;
        try {
            signature = decodeSignature(signatureHeader);
        } catch (DecoderException e) {
            logger.error("Invalid signature: {}", signatureHeader);
            return false;
        }

        byte[] expectedSigature = calculateExpectedSignature(body, webhookSecret);

        String expectedSignatureString = new String(Hex.encodeHex(expectedSigature));
        System.out.println("signagtureHeader:" + signatureHeader);
        System.out.println("ExpectedSignagure:" + expectedSignatureString);
        return MessageDigest.isEqual(signature, expectedSigature);
    }

    private byte[] decodeSignature(String signatureHeader) throws DecoderException {
        byte[] signature;
        signature = Hex
            .decodeHex(signatureHeader.substring(SIGNATURE_PREFIX.length()).toCharArray());
        return signature;
    }

    @VisibleForTesting
    public byte[] calculateExpectedSignature(String body, String webhookSecret) {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        return calculateExpectedSignature(payload, webhookSecret);
    }


    private byte[] calculateExpectedSignature(byte[] payload, String webhookSecret) {
        SecretKeySpec key = new SecretKeySpec(webhookSecret.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac hmac;
        try {
            hmac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            hmac.init(key);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hmac SHA1 must be supported", e);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException("Hmac SHA1 must be compatible to Hmac SHA1 Secret Key",
                e);
        }
        return hmac.doFinal(payload);
    }


    private String readSecretsKey() throws IOException {
        initializeAmazonSecrets();
        SecretsReader secretsReader = new SecretsReader();
        String secretValue = secretsReader.readAuthFromSecrets(amazonSecretName, amazonSecretKey);
        return secretValue;

    }


    private void initializeAmazonSecrets() {
        this.amazonSecretName = environment.readEnvOpt(SECRET_NAME)
            .orElseThrow(
                () -> new IllegalStateException("Missing env variable:" + SECRET_NAME));
        this.amazonSecretKey = environment.readEnvOpt(SECRET_KEY)
            .orElseThrow(() -> new IllegalStateException("Missing env variable:" + SECRET_KEY));
    }


}
