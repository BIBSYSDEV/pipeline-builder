package no.bibsys.aws.secrets;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SignatureChecker uses a secret key and the body of the request to generate a validation
 * signature. It expects the client not to send the api-key, but to send a sha1 hash value of the
 * concatenation of the secret value and the request body. In short it works as follows:
 * <p>
 * <b>Input:</b>
 * <ul>
 * <li>{@code secret}: Secret value stored in AWS Secret Manager </li>
 * <li>{@code request-body} : The request body sent by a (REST) client</li>
 * <li>{@code client-signature}: An sha1 hash value for the concatenation of  the {@code secret}
 * and the {@code request-body} </li>
 * </ul>
 * <br/>
 * <b>Output:</b>
 * <ul>
 * <li>true if the client's signature matches the signature calculated by {@link
 * SignatureChecker}</li>
 * <li>false if the client's signature does not  matche the signature calculated by {@link
 * SignatureChecker}</li>
 * </ul>
 *
 *
 * This class is used mainly to decode the Signature sent by Github webhooks.
 *
 * </p>
 */
public class SignatureChecker {

    private static final String SIGNATURE_PREFIX = "sha1=";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final Logger logger = LoggerFactory.getLogger(SignatureChecker.class);

    public static String AWS_SECRET_NAME = "SECRET_NAME";
    public static String AWS_SECRET_KEY = "SECRET_KEY";

    private transient SecretsReader secretsReader;


    public SignatureChecker(String secretName, String secretKey) {
        secretsReader = new SecretsReader(secretName, secretKey);
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
                "webhookSecret not configured. Skip signature validation");
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
        logger.info("signagtureHeader:" + signatureHeader);
        logger.info("ExpectedSignagure:" + expectedSignatureString);
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
        String secretValue = secretsReader.readSecret();
        return secretValue;

    }

    public void setSecretsReader(SecretsReader secretsReader) {
        this.secretsReader = secretsReader;
    }


}
