package no.bibsys.cloudformation.helpers;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import java.util.List;
import java.util.stream.Collectors;

public class StackResources {


    private final transient String stackName;

    public StackResources(String stackName) {
        this.stackName = stackName;
    }


    public List<StackResource> getResources(ResourceType resourceType) {
        AmazonCloudFormation client = AmazonCloudFormationClientBuilder.defaultClient();
        DescribeStackResourcesResult resutl = client
            .describeStackResources(new DescribeStackResourcesRequest().withStackName(stackName));
        return resutl.getStackResources().stream()
            .filter(resource -> resource.getResourceType().equals(resourceType.toString()))
            .collect(Collectors.toList());

    }

}
