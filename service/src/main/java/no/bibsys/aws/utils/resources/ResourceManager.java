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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceManager {

    private static final Logger logger= LoggerFactory.getLogger(ResourceManager.class);

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
        return new SwaggerHubUpdater(apiGateway,
            apiGatewayRestApi,
            swaggerHubInfo,
            stage,
            stackName,
            gitInfo);
    }


    protected StaticUrlInfo initStaticUrlInfo(StaticUrlInfo staticUrlInfo, String gitBranch) {

        StaticUrlInfo newStaticUrlInfo = staticUrlInfo;
        logger.info("Gitbranch:{}",gitBranch);
        if (!gitBranch.equals(GitConstants.MASTER)) {
            String randomString = DigestUtils.sha1Hex(gitBranch).substring(0, 5);
            logger.info("RandomString:{}",randomString);
            String newUrl = String.format("%s.%s", randomString, staticUrlInfo.getRecordSetName());
            newStaticUrlInfo=new StaticUrlInfo(staticUrlInfo.getZoneName(), newUrl,
                staticUrlInfo.getStage());
        }
        if(staticUrlInfo.getStage().equals(Stage.TEST)){
            newStaticUrlInfo=new StaticUrlInfo(newStaticUrlInfo.getZoneName(),
                "test."+newStaticUrlInfo.getRecordSetName(),Stage.TEST);

        }
        return newStaticUrlInfo;


    }
}
