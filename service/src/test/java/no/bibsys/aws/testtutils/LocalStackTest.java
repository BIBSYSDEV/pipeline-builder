package no.bibsys.aws.testtutils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BasePathMapping;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsRequest;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsResult;
import com.amazonaws.services.apigateway.model.GetDomainNameRequest;
import com.amazonaws.services.apigateway.model.GetDomainNameResult;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.AmazonCloudFormationException;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ListStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.cloudformation.model.StackSummary;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.DescribeLogGroupsResult;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.git.github.GithubConf;
import no.bibsys.aws.lambda.EnvironmentConstants;
import no.bibsys.aws.lambda.api.handlers.GithubHandler;
import no.bibsys.aws.secrets.GithubSignatureChecker;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.JsonUtils;
import org.apache.http.HttpStatus;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class LocalStackTest {

    public static final Region ARBITRARY_REGION = Region.getRegion(Regions.EU_WEST_1);
    public static final String APPROVE_ALL_KEYS = null;
    private static final int ENV_VARIABLE_NAME = 0;
    private static final String ARBITRARY_SECRET_KEY = "secretKey";

    protected static final String TEST_STACK = "testStack";
    private static final String SOME_REPO_OWNER = "owner";
    private static final String SOME_REPO = "repo";
    private static final String SOME_GIT_BRANCH = "branch";
    private static final int REQUEST = 0;
    private static final String ARBITRARY_BUCKET_VERSION = "v1";
    private static final String FINAL_STACK = "finalStack";
    private static final String PIPELINE_STACK = "pipelineStack";
    private static final String ARBITRARY_BUCKET_NAME = "aBucket";
    private static final String ARBITRARY_BUCKET_ARN = "s3:::" + ARBITRARY_BUCKET_NAME;
    private static final String REST_API_PHYSICAL_ID = "aws:::RestAPI";
    private static final String MOCK_REGIONAL_URL = "regional";
    private static final String MOCK_HOSTED_ZONE_ID = "mock.hosted.zone";
    private static final String CALLER_ID = "callerId";
    private static final String MOCK_REST_API_ID = "mockRestApiID";
    private static final String MOCK_BASEPATH_PREFIX = "basepath.for.";
    private static final String OPENAPI_RESOURCES_FOLDER = "openapi";
    private static final String OPENAPI_FILE = "openapi.yml";

    protected final transient PipelineStackConfiguration pipelineStackConfiguration;

    public LocalStackTest() {
        GithubConf gitInfo = new GithubConf(SOME_REPO_OWNER, SOME_REPO, SOME_GIT_BRANCH,
            mockSecretsReader());
        pipelineStackConfiguration = new PipelineStackConfiguration(gitInfo);
    }

    public static SecretsReader mockSecretsReader() {
        return () -> ARBITRARY_SECRET_KEY;
    }

    public static SecretsReader mockSecretsReader(String secretKey) {
        return () -> secretKey;
    }

    public static Environment mockEnvironment() {
        return mockEnvironment(Collections.emptyMap());
    }

    public static Environment mockEnvironment(Map<String, String> mockEnvValues) {
        Map<String, String> newMap = new ConcurrentHashMap<>();
        newMap.put(EnvironmentConstants.AWS_REGION, ARBITRARY_REGION.getName());
        newMap.putAll(mockEnvValues);

        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv(anyString()))
            .then(invocation -> {
                String envVariable = invocation.getArgument(ENV_VARIABLE_NAME);
                return newMap.get(envVariable);
            });
        return environment;
    }

    public static Environment mockEnvironment(String envVariable, String value) {
        return mockEnvironment(Collections.singletonMap(envVariable, value));
    }

    public static GithubHandler getGithubHandlerWithMockSecretsReader(Environment environment) {

        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(mockSecretsReader());
        return new GithubHandler(environment,
            Mockito.mock(AmazonCloudFormation.class),
            Mockito.mock(AmazonS3.class),
            Mockito.mock(AWSLambda.class),
            Mockito.mock(AWSLogs.class),
            signatureChecker,
            mockSecretsReader(),
            mockSecretsReader()
        );
    }

    protected AmazonRoute53 initializeRoute53Client(String zoneName) {
        AmazonRoute53 route53 = Mockito.mock(AmazonRoute53.class);
        when(route53.listHostedZones()).thenReturn(listHostedZonesResult(zoneName));
        return route53;
    }

    private ListHostedZonesResult listHostedZonesResult(String zoneName) {

        HostedZone hostedZone = new HostedZone(MOCK_HOSTED_ZONE_ID,
            zoneName, CALLER_ID);
        return new ListHostedZonesResult()
            .withHostedZones(hostedZone);
    }

    protected AWSLambda initializeLambdaClient() {
        AWSLambda lambdaClient = Mockito.mock(AWSLambda.class);
        when(lambdaClient.invoke(any())).thenAnswer(invocation -> new InvokeResult()
            .withStatusCode(HttpStatus.SC_OK));
        return lambdaClient;
    }

    protected AWSLogs initializeMockLogsClient() {
        AWSLogs logsClient = Mockito.mock(AWSLogs.class);
        when(logsClient.describeLogGroups()).thenReturn(new DescribeLogGroupsResult()
            .withLogGroups(Collections.emptyList()));
        return logsClient;
    }

    public AmazonCloudFormation initializeMockCloudFormation() {
        AmazonCloudFormation cloudFormation = Mockito.mock(AmazonCloudFormation.class);

        when(cloudFormation.listStacks()).thenReturn(listWithStackSummaries());
        when(cloudFormation.describeStacks()).thenReturn(describeStackResults());
        when(cloudFormation.deleteStack(any())).then(invocation -> new DeleteStackResult());
        when(cloudFormation.describeStackResources(any()))
            .then(describeStackResourcesResultAnswer());

        return cloudFormation;
    }

    public AmazonCloudFormation mockCloudFormationwithNoStack() {
        AmazonCloudFormation cloudFormation = Mockito.mock(AmazonCloudFormation.class);
        when(cloudFormation.describeStackResources(any())).thenThrow(AmazonCloudFormationException.class);
        when(cloudFormation.deleteStack(any())).thenThrow(AmazonCloudFormationException.class);
        return cloudFormation;
    }

    protected AmazonApiGateway initializeAmazonApiGateway() throws IOException {
        AmazonApiGateway apiGateway = Mockito.mock(AmazonApiGateway.class);
        when(apiGateway.getDomainName(any()))
            .then((Answer<GetDomainNameResult>) this::getDomainNameResult);
        when(apiGateway.getBasePathMappings(any())).then(this::getBasePathMappingResult);
        when(apiGateway.getExport(any())).thenReturn(getExportResult());
        return apiGateway;
    }

    private GetExportResult getExportResult() throws IOException {
        String jsonApi = JsonUtils.yamlToJson(IoUtils
            .resourceAsString(Paths.get(OPENAPI_RESOURCES_FOLDER, OPENAPI_FILE)));
        ByteBuffer buffer = ByteBuffer.wrap(jsonApi.getBytes(), 0, jsonApi.getBytes().length);
        return new GetExportResult().withBody(buffer);
    }

    private GetBasePathMappingsResult getBasePathMappingResult(InvocationOnMock invocation) {
        GetBasePathMappingsRequest request = invocation.getArgument(REQUEST);
        String domainName = request.getDomainName();
        BasePathMapping basepathMapping = new BasePathMapping()
            .withBasePath(MOCK_BASEPATH_PREFIX + domainName)
            .withRestApiId(MOCK_REST_API_ID);
        return new GetBasePathMappingsResult()
            .withItems(basepathMapping);
    }

    private GetDomainNameResult getDomainNameResult(InvocationOnMock invocation) {
        GetDomainNameRequest request = invocation.getArgument(REQUEST);
        String regionalDomainName = MOCK_REGIONAL_URL + request.getDomainName();
        return new GetDomainNameResult().withRegionalDomainName(regionalDomainName);
    }

    protected AmazonS3 initializeS3() {
        AmazonS3 s3 = Mockito.mock(AmazonS3.class);
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
            StackResource restApiResource = new StackResource()
                .withStackName(request.getStackName())
                .withResourceType(ResourceType.REST_API.toString())
                .withPhysicalResourceId(REST_API_PHYSICAL_ID);
            return new DescribeStackResourcesResult()
                .withStackResources(bucketResource, restApiResource);
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
