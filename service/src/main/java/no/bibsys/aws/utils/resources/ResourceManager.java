package no.bibsys.aws.utils.resources;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.kms.model.NotFoundException;
import java.io.IOException;
import java.util.Optional;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.lambda.deploy.handlers.SwaggerHubUpdater;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.utils.constants.GitConstants;
import org.apache.commons.codec.digest.DigestUtils;

public class ResourceManager {


    protected String findRestApi(String stackId) {
        StackResources stackResources = new StackResources(stackId);
        Optional<String> restApiId = stackResources.getResources(ResourceType.REST_API).stream()
            .map(resource -> resource.getPhysicalResourceId()).findAny();
        return restApiId
            .orElseThrow(() -> new NotFoundException("Could not find an API Gateway Rest API"));

    }

    protected SwaggerHubUpdater initSwaggerHubUpdater(String stackName,
        SwaggerHubInfo swaggerHubInfo,
        Stage stage, GitInfo gitInfo, AmazonApiGateway apiGateway, String apiGatewayRestApi)
        throws IOException {
        return new SwaggerHubUpdater(apiGateway
            , apiGatewayRestApi,
            swaggerHubInfo,
            stage,
            stackName,
            gitInfo);
    }


    protected StaticUrlInfo initStaticUrlInfo(StaticUrlInfo staticUrlInfo, String gitBranch) {

        if (gitBranch.equals(GitConstants.MASTER)) {
            return staticUrlInfo;
        } else {

            String randomString = DigestUtils.sha1Hex(gitBranch).substring(0, 5);
            String newUrl = String.format("%s.%s", randomString, staticUrlInfo.getRecordSetName());
            return new StaticUrlInfo(staticUrlInfo.getDomainName(), newUrl,
                staticUrlInfo.getStage());
        }


    }
}
