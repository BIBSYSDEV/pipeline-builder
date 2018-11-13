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

    public SecretsReader(AWSSecretsManager client){
        this.client=client;
    }


    public SecretsReader(){
        this.client=AWSSecretsManagerClientBuilder.standard()
            .withRegion(Region.EU_Ireland.toString())
            .build();
    }


    public String readAuthFromSecrets(String secretName,String secretKey) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        Optional<GetSecretValueResult> getSecretValueResult = readAuthKey(client,secretName);

        if (getSecretValueResult.map(result -> result.getSecretString()).isPresent()) {
            String secret = getSecretValueResult.get().getSecretString();
            String value = mapper.readTree(secret)
                .findValuesAsText(secretKey).stream().findFirst().orElse(null);
            return value;
        }
        return null;
    }


    private Optional<GetSecretValueResult> readAuthKey(AWSSecretsManager client,String secretName) {
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
