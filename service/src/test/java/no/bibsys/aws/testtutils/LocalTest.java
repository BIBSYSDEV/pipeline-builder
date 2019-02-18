package no.bibsys.aws.testtutils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.s3.AmazonS3;
import java.util.Collections;
import java.util.Map;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import org.apache.http.HttpStatus;
import org.mockito.Mockito;

public class LocalTest {

    public static final Region ARBITRARY_REGION = Region.getRegion(Regions.EU_WEST_1);
    private static final int ENV_VARIABLE_NAME = 0;

    public static AmazonCloudFormation mockCloudFormationClient() {
        AmazonCloudFormation cloudFormation = Mockito.mock(AmazonCloudFormation.class);
        return cloudFormation;
    }

    public static AmazonApiGateway mockApiGateway() {
        AmazonApiGateway amazonApiGateway = Mockito.mock(AmazonApiGateway.class);
        return amazonApiGateway;
    }

    public static AmazonS3 mockS3Client() {
        AmazonS3 client = Mockito.mock(AmazonS3.class);
        return client;
    }

    public static AWSLambda mockLambdaClient() {
        AWSLambda lambdaClient = Mockito.mock(AWSLambda.class);
        when(lambdaClient.invoke(any())).thenAnswer(invocation -> new InvokeResult()
            .withStatusCode(HttpStatus.SC_OK));
        return lambdaClient;
    }

    public static AWSLogs mockLogsClient() {
        AWSLogs logsClient = Mockito.mock(AWSLogs.class);
        return logsClient;
    }

    public static SecretsReader mockSecretsReader() {
        return () -> "secretKey";
    }

    public static Environment mockEnvironment(String envVariable, String value) {
        return mockEnvironment(Collections.singletonMap(envVariable, value));
    }

    public static Environment mockEnvironment(Map<String, String> mockEnvValues) {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv(anyString()))
            .then(invocation -> {
                String envVariable = invocation.getArgument(ENV_VARIABLE_NAME);
                return mockEnvValues.get(envVariable);
            });
        return environment;
    }
}
