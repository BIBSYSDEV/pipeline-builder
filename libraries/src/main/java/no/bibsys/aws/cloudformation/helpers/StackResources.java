package no.bibsys.aws.cloudformation.helpers;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StackResources {


    private final transient String stackName;

    public StackResources(String stackName) {
        this.stackName = stackName;
    }


    private Stream<StackResource> getResourcesStream(ResourceType resourceType) {
        AmazonCloudFormation client = AmazonCloudFormationClientBuilder.defaultClient();
        DescribeStackResourcesResult result = client
            .describeStackResources(new DescribeStackResourcesRequest().withStackName(stackName));
        return result.getStackResources().stream()
            .filter(resource -> resource.getResourceType().equals(resourceType.toString()));

    }


    public List<StackResource> getResources(ResourceType resourceType) {
        return getResourcesStream(resourceType).collect(Collectors.toList());
    }


    public List<String> getResourceIds(ResourceType resourceType) {
        return getResourcesStream(resourceType).map(StackResource::getPhysicalResourceId)
            .collect(Collectors.toList());
    }


}
