package no.bibsys.aws.testtutils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import java.util.Collections;
import java.util.Map;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
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
