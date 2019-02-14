package no.bibsys.aws.testtutils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import org.mockito.Mockito;

public class LocalTest {

    public static AmazonCloudFormation mockCloudFormationClient(){
        AmazonCloudFormation cloudFormation= Mockito.mock(AmazonCloudFormation.class);
        return cloudFormation;
    }

    public static AmazonApiGateway mockApiGateway(){
        AmazonApiGateway amazonApiGateway= Mockito.mock(AmazonApiGateway.class);
        return amazonApiGateway;
    }

    public static SecretsReader mockSecretsReader(){
        return ()->"secretKey";
    }



    public static Environment mockEnvironment(){
        Environment environment=Mockito.mock(Environment.class);
        when(environment.readEnv(anyString()))
            .then(invocation -> invocation.<String>getArgument(0));
        return environment;
    }


}
