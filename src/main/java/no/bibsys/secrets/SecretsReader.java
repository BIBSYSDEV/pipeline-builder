package no.bibsys.secrets;

import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.utils.JsonUtils;

public class SecretsReader {


    private final transient AWSSecretsManager client;
    private final transient String secretName;
    private final transient String secretKey;

    public SecretsReader(AWSSecretsManager client, String secretName, String secretKey) {
        this.client=client;
        this.secretKey = secretKey;
        this.secretName = secretName;
    }


    public SecretsReader(String secretName, String secretKey) {
        this(AWSSecretsManagerClientBuilder.standard()
            .withRegion(Region.EU_Ireland.toString())
                .build(),
            secretName,
            secretKey);

    }


    public String readSecret() throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        Optional<GetSecretValueResult> getSecretValueResult = readAuthKey();

        if (getSecretValueResult.map(result -> result.getSecretString()).isPresent()) {
            String secret = getSecretValueResult.get().getSecretString();
            String value = mapper.readTree(secret)
                .findValuesAsText(secretKey).stream().findFirst().orElse(null);
            return value;
        }
        return null;
    }


    private Optional<GetSecretValueResult> readAuthKey() {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
            .withSecretId(secretName);
        Optional<GetSecretValueResult> getSecretValueResult = Optional.empty();
        try {
            getSecretValueResult = Optional.ofNullable(client
                .getSecretValue(getSecretValueRequest));
        } catch (InvalidRequestException e) {
            getSecretValueResult = Optional.empty();
        }
        return getSecretValueResult;
    }

}
