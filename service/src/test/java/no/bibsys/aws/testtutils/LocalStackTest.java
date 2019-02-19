package no.bibsys.aws.testtutils;

import static no.bibsys.aws.testtutils.LocalTest.mockCloudFormationClient;
import static no.bibsys.aws.testtutils.LocalTest.mockLambdaClient;
import static no.bibsys.aws.testtutils.LocalTest.mockLogsClient;
import static no.bibsys.aws.testtutils.LocalTest.mockS3Client;
import static no.bibsys.aws.testtutils.LocalTest.mockSecretsReader;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ListStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.StackSummary;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.DescribeLogGroupsResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import java.util.Collections;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.utils.stacks.StackWiper;
import org.mockito.stubbing.Answer;

public class LocalStackTest {

    private static final String SOME_REPO_OWNER = "owner";
    private static final String SOME_REPO = "repo";
    private static final String SOME_GIT_BRANCH = "branch";
    private static final int REQUEST = 0;
    private static final String ARBITRARY_BUCKET_VERSION = "v1";
    private static final String TEST_STACK = "testStack";
    private static final String FINAL_STACK = "finalStack";
    private static final String PIPELINE_STACK = "pipelineStack";
    private static final String ARBITRARY_BUCKET_NAME = "aBucket";
    private static final String ARBITRARY_BUCKET_ARN = "s3:::" + ARBITRARY_BUCKET_NAME;

    protected final transient StackWiper stackWiper;
    private final transient PipelineStackConfiguration pipelineStackConfiguration;

    public LocalStackTest() {
        GithubConf gitInfo = new GithubConf(SOME_REPO_OWNER, SOME_REPO, SOME_GIT_BRANCH,
            mockSecretsReader());
        pipelineStackConfiguration = new PipelineStackConfiguration(gitInfo);
        AmazonCloudFormation acf = initializeMockCloudFormation();
        AmazonS3 s3 = initializeS3();
        AWSLambda lambda = initializeLambdaClient();
        AWSLogs logsClient = initializeMockLogsClient();
        this.stackWiper = new StackWiper(pipelineStackConfiguration, acf,
            s3, lambda, logsClient);
    }

    protected AWSLambda initializeLambdaClient() {
        return mockLambdaClient();
    }

    protected AWSLogs initializeMockLogsClient() {
        AWSLogs logsClient = mockLogsClient();
        when(logsClient.describeLogGroups()).thenReturn(new DescribeLogGroupsResult()
            .withLogGroups(Collections.emptyList()));
        return logsClient;
    }

    protected AmazonCloudFormation initializeMockCloudFormation() {
        AmazonCloudFormation cloudFormation = mockCloudFormationClient();
        when(cloudFormation.listStacks()).thenReturn(listWithStackSummaries());
        when(cloudFormation.describeStacks()).thenReturn(describeStackResults());
        when(cloudFormation.deleteStack(any())).then(invocation -> new DeleteStackResult());
        when(cloudFormation.describeStackResources(any()))
            .then(describeStackResourcesResultAnswer());

        return cloudFormation;
    }

    protected AmazonS3 initializeS3() {
        AmazonS3 s3 = mockS3Client();
        when(s3.listVersions(any()))
            .then(invocation -> listVersionsAnswer())
            .thenReturn(new VersionListing());

        when(s3.listObjects(anyString()))
            .then((Answer<ObjectListing>) invocation -> objectListingAnswer())
            .thenReturn(new ObjectListing());
        return s3;
    }

    private ObjectListing objectListingAnswer() {
        ObjectListing objectList = new ObjectListing();
        objectList.setTruncated(false);
        S3ObjectSummary objectSummary = new S3ObjectSummary();
        objectSummary.setBucketName(ARBITRARY_BUCKET_NAME);
        objectList.getObjectSummaries().add(objectSummary);
        return objectList;
    }

    private VersionListing listVersionsAnswer() {
        S3VersionSummary versionSummary = new S3VersionSummary();
        versionSummary.setVersionId(ARBITRARY_BUCKET_VERSION);
        versionSummary.setBucketName(ARBITRARY_BUCKET_NAME);
        versionSummary.setIsLatest(true);
        VersionListing vl = new VersionListing();
        vl.setTruncated(false);
        vl.setVersionSummaries(Collections.singletonList(versionSummary));
        return vl;
    }

    private Answer<DescribeStackResourcesResult> describeStackResourcesResultAnswer() {
        return invocation -> {
            DescribeStackResourcesRequest request = invocation.getArgument(REQUEST);
            StackResource bucketResource = new StackResource()
                .withStackName(request.getStackName())
                .withResourceType(ResourceType.S3_BUCKET.toString())
                .withPhysicalResourceId(ARBITRARY_BUCKET_ARN);
            return new DescribeStackResourcesResult()
                .withStackResources(bucketResource);
        };
    }

    private ListStacksResult listWithStackSummaries() {
        StackSummary testStackSummary = new StackSummary().withStackName(pipelineStackConfiguration
            .getPipelineConfiguration().getTestServiceStack());
        StackSummary finalStackSummary = new StackSummary().withStackName(pipelineStackConfiguration
            .getPipelineConfiguration().getFinalServiceStack());
        StackSummary pipelineStackSummary = new StackSummary()
            .withStackName(pipelineStackConfiguration
                .getPipelineStackName());

        return new ListStacksResult()
            .withStackSummaries(testStackSummary, finalStackSummary, pipelineStackSummary);
    }

    private DescribeStacksResult describeStackResults() {
        return new DescribeStacksResult()
            .withStacks(new Stack().withStackName(TEST_STACK),
                new Stack().withStackName(FINAL_STACK),
                new Stack().withStackName(PIPELINE_STACK)
            );
    }
}
