package no.bibsys.certificates;

import com.amazonaws.services.apigateway.model.NotFoundException;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;

public class CertificatesManager {

//    private static final String CERTIFICATE_SSM_PARAMETER = "infrastructure-entitydata-certificate-arn";


    public String certificateArn(String certificateName) {
            return readParameter(certificateName);
    }


    public String readParameter(String parameterName) {
        try {
            AWSSimpleSystemsManagement ssm = AWSSimpleSystemsManagementClientBuilder
                .defaultClient();
            GetParameterResult requestResult = ssm
                .getParameter(new GetParameterRequest().withName(parameterName));
            Parameter parameter = requestResult.getParameter();
            String result = parameter.getValue();
            return result;
        } catch ( NotFoundException e) {
            throw new NotFoundException(
                "Failed to read SSM parameter:" + parameterName);
        }


    }


}
