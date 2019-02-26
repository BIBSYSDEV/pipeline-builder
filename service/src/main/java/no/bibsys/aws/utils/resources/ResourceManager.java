package no.bibsys.aws.utils.resources;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.kms.model.NotFoundException;
import java.util.Optional;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.lambda.deploy.handlers.SwaggerHubUpdater;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.swaggerhub.SwaggerHubInfo;
import no.bibsys.aws.utils.constants.GitConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    private static final int RANDOM_STRING_LENGTH = 5;
    protected static final int ADEQUATELY_RANDOM_STRING = RANDOM_STRING_LENGTH;
    private final transient AmazonCloudFormation cloudFormation;

    public ResourceManager(AmazonCloudFormation cloudFormation) {
        this.cloudFormation = cloudFormation;
    }

    protected String findRestApi(String stackId) {
        StackResources stackResources = new StackResources(stackId, cloudFormation);
        Optional<String> restApiId = stackResources.getResources(ResourceType.REST_API).stream()
            .map(StackResource::getPhysicalResourceId).findAny();
        return restApiId
            .orElseThrow(() -> new NotFoundException("Could not find an API Gateway Rest API"));
    }

    protected SwaggerHubUpdater initSwaggerHubUpdater(String stackName,
        SwaggerHubInfo swaggerHubInfo, Stage stage, GitInfo gitInfo, AmazonApiGateway apiGateway,
        String apiGatewayRestApi, SecretsReader secretsReader) {
        return new SwaggerHubUpdater(apiGateway,
            apiGatewayRestApi,
            swaggerHubInfo,
            secretsReader,
            stage,
            stackName,
            gitInfo);
    }

    protected StaticUrlInfo initStaticUrlInfo(StaticUrlInfo staticUrlInfo, String gitBranch) {

        StaticUrlInfo newStaticUrlInfo = staticUrlInfo;
        logger.info("Gitbranch:{}", gitBranch);

        if (!gitBranch.equals(GitConstants.MASTER)) {
            String randomString = DigestUtils.sha1Hex(gitBranch).substring(0, ADEQUATELY_RANDOM_STRING);
            logger.info("RandomString:{}", randomString);
            String newUrl = String.format("%s.%s", randomString, newStaticUrlInfo.getRecordSetName());
            newStaticUrlInfo = new StaticUrlInfo(newStaticUrlInfo.getZoneName(), newUrl,
                staticUrlInfo.getStage());
        }
        if (staticUrlInfo.getStage().equals(Stage.TEST)) {
            newStaticUrlInfo = new StaticUrlInfo(newStaticUrlInfo.getZoneName(),
                "test." + staticUrlInfo.getRecordSetName(), Stage.TEST);
        }

        return newStaticUrlInfo;
    }
}
