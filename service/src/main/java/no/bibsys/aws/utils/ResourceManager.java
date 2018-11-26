package no.bibsys.aws.utils;

import com.amazonaws.services.kms.model.NotFoundException;
import java.util.Optional;
import no.bibsys.aws.cloudformation.PipelineConfiguration;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;
import no.bibsys.aws.git.github.GitInfo;

public class ResourceManager {


    protected String findRestApi(GitInfo gitInfo, Stage stage) {
        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration(
            gitInfo.getRepository(), gitInfo.getBranch());
        String stackName = pipelineConfiguration.getCurrentServiceStackName(stage);
        StackResources stackResources = new StackResources(stackName);
        Optional<String> restApiId = stackResources
            .getResources(ResourceType.REST_API).stream()
            .map(resource -> resource.getPhysicalResourceId())
            .findAny();
        return restApiId
            .orElseThrow(() -> new NotFoundException("Could not find an API Gateway Rest API"));

    }
}
