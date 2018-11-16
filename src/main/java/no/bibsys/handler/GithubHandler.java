package no.bibsys.handler;


import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import no.bibsys.Application;
import no.bibsys.git.github.GitInfo;
import no.bibsys.git.github.GithubConf;
import no.bibsys.handler.requests.PullRequest;
import no.bibsys.handler.requests.PushEvent;
import no.bibsys.handler.requests.RepositoryInfo;
import no.bibsys.handler.templates.ApiGatewayHandlerTemplate;
import no.bibsys.secrets.SecretsReader;
import no.bibsys.utils.Environment;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GithubHandler extends ApiGatewayHandlerTemplate<String, String> {

    private static final String SIGNATURE_PREFIX = "sha1=";
    private static String SECRET_NAME = "SECRET_NAME";
    private static String SECRET_KEY = "SECRET_KEY";


    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static final Logger logger = LogManager.getLogger(GithubHandler.class);

    private transient String amazonSecretName;
    private transient String amazonSecretKey;
    private transient Environment environment;


    public GithubHandler() {
        super(String.class);
    }

    @Override
    public String processInput(String request, Map<String, String> headers, Context context)
        throws IOException {
        init();
        Optional<RepositoryInfo> gitEventOpt=parseEvent(request);

        String webhookSecurityToken = headers.get("X-Hub-Signature");
        verifySecurityToken(webhookSecurityToken, request);
        String response="No action";
        if(gitEventOpt.isPresent()){
            RepositoryInfo repositoryInfo =gitEventOpt.get();
               if(repositoryInfo instanceof PullRequest){
                   response=processPullRequest((PullRequest) repositoryInfo);
               }
               else if(repositoryInfo instanceof PushEvent){
                   response=processPushEvent((PushEvent) repositoryInfo);
               }
        }
        return response;
    }




    private String processPushEvent(PushEvent pushEvent) throws IOException {
        return pushEvent.toString();

    }


    private String processPullRequest(PullRequest pullRequest) throws IOException {
        if (pullRequest.getAction().equals(PullRequest.ACTION_OPEN)
            || pullRequest.getAction().equals(PullRequest.ACTION_REOPEN)) {
            createStacks(pullRequest);
        }

        if (pullRequest.getAction().equals(PullRequest.ACTION_CLOSE)) {
            deleteStacks(pullRequest);
        }

        System.out.println(pullRequest.toString());
        logger.info(pullRequest.toString());

        return  pullRequest.toString();



    }


    private Optional<RepositoryInfo> parseEvent(String json) throws IOException {
        Optional<RepositoryInfo> event= PullRequest.create(json);
        if(!event.isPresent()) {
            event = PushEvent.create(json);
        }
        return event;
    }



    protected void deleteStacks(RepositoryInfo repositoryInfo)
        throws IOException {
        GitInfo gitInfo=new GithubConf(repositoryInfo.getOwner(),repositoryInfo.getRepository(),new Environment());
        Application application = new Application(gitInfo,repositoryInfo.getBranch());
        application.wipeStacks();
    }

    protected void createStacks(RepositoryInfo repositoryInfo)
        throws IOException {
        GitInfo gitInfo=new GithubConf(repositoryInfo.getOwner(),repositoryInfo.getRepository(),new Environment());
        Application application = new Application(gitInfo,repositoryInfo.getBranch());
        application.createStacks();
    }


    private boolean verifySecurityToken(String token, String requestBody) throws IOException {
        String privateKey = readSecretsKey();
        return validateSignature(token, requestBody, null, privateKey);
    }


    @VisibleForTesting
    public boolean validateSignature(String signatureHeader, String body, String encoding,
        String webhookSecret)
        throws UnsupportedEncodingException {

//        if (webhookSecret == null || webhookSecret.equals("")) {
//            logger.debug(
//                "{}.webhookSecret not configured. Skip signature validation");
//            return true;
//        }

        byte[] signature;
        try {
            signature = Hex
                .decodeHex(signatureHeader.substring(SIGNATURE_PREFIX.length()).toCharArray());
        } catch (DecoderException e) {
            logger.error("Invalid signature: {}", signatureHeader);
            return false;
        }
        byte[] payload = body.getBytes(encoding == null ? "UTF-8" : encoding);
        byte[] expectedSignature = getExpectedSignature(payload, webhookSecret);

        String signatureFrommHeader = new String(Hex.encodeHex(signature));
        String expectedSignatureString = new String(Hex.encodeHex(expectedSignature));

        System.out.println("signagtureHeader:" + signatureHeader);
        System.out.println("signagtureFromHeader:" + signatureFrommHeader);
        System.out.println("ExpectectedSignagure:" + expectedSignatureString);
        return MessageDigest.isEqual(signature, expectedSignature);
    }


    @VisibleForTesting
    public byte[] getExpectedSignature(byte[] payload, String webhookSecret) {
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





    private void init() {
        if (environment == null) {
            environment = new Environment();
        }

    }

    private void initializeAmazonSecrets() {
        this.amazonSecretName = environment.readEnvOpt(SECRET_NAME)
            .orElseThrow(
                () -> new IllegalStateException("Missing env variable:" + SECRET_NAME));
        this.amazonSecretKey = environment.readEnvOpt(SECRET_KEY)
            .orElseThrow(() -> new IllegalStateException("Missing env variable:" + SECRET_KEY));
    }


    private String readSecretsKey() throws IOException {
        initializeAmazonSecrets();
        SecretsReader secretsReader = new SecretsReader();
        String secretValue = secretsReader.readAuthFromSecrets(amazonSecretName, amazonSecretKey);
        return secretValue;

    }


    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


}


