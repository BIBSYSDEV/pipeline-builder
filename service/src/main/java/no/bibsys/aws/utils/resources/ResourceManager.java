package no.bibsys.aws.utils.resources;

import com.amazonaws.services.kms.model.NotFoundException;
import java.util.Optional;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;

public class ResourceManager {


    protected String findRestApi(String stackId) {
        StackResources stackResources = new StackResources(stackId);
        Optional<String> restApiId = stackResources.getResources(ResourceType.REST_API).stream()
                .map(resource -> resource.getPhysicalResourceId()).findAny();
        return restApiId.orElseThrow(() -> new NotFoundException("Could not find an API Gateway Rest API"));

    }
}
