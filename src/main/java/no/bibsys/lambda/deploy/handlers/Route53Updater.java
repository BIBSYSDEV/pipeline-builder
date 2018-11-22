package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.RestApi;
import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.stream.Collectors;
import no.bibsys.apigateway.ApiGatewayApiInfo;
import no.bibsys.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.constants.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Route53Updater {

    public static final String ZONE_NAME_ENV = "ZONE_NAME";
    public static final String REPOSITORY_NAME_ENV_VAR = "REPOSITORY";
    public static final String BRANCH_NAME_ENV_VAR = "BRANCH";
    public static final String CERTIFICATE_ARN = "REGIONAL_CERTIFICATE_ARN";
    private static final Logger log = LoggerFactory.getLogger(Route53Updater.class);
    private final transient String zoneName;
    private final transient String repository;
    private final transient String branch;
    private final transient ApiGatewayApiInfo apiGatewayApiInfo;
    private final transient AmazonApiGateway apiGatewayClient;


    private final transient ApiGatewayBasePathMapping apiGatewayBasePathMapping;
    private transient final String recordSetName;


    private transient AmazonRoute53 route53Client;


    public Route53Updater(String zonName,
        String repository,
        String branch,
        Stage stage,
        AmazonApiGateway apiGatewayClient) {

        NetworkConstants networkConstants = new NetworkConstants(stage);
        this.repository = repository;
        this.recordSetName = networkConstants.getRecordSetName();
        this.branch = branch;
        this.apiGatewayClient = apiGatewayClient;
        this.zoneName = zonName;

        this.route53Client = AmazonRoute53ClientBuilder.defaultClient();
        CloudFormationConfigurable conf = new CloudFormationConfigurable(repository, branch);
        this.apiGatewayApiInfo = new ApiGatewayApiInfo(conf, stage.toString(), apiGatewayClient);
        this.apiGatewayBasePathMapping = new ApiGatewayBasePathMapping(
            apiGatewayClient,
            networkConstants.getDomainName(),
            stage);
    }


    public Route53Updater copy(Stage stage) {
        return new Route53Updater(zoneName, repository, branch, stage, apiGatewayClient);
    }


    public ChangeResourceRecordSetsResult updateServerUrl(String certificateArn) {
        RestApi restApi = apiGatewayApiInfo.findRestApi()
            .orElseThrow(() -> new NotFoundException("GatewayApi or GatewayApi stage not found"));
        apiGatewayBasePathMapping.createBasePath(restApi, certificateArn);

        String targetDomainName = apiGatewayBasePathMapping.getTargeDomainName();
        return route53Client.changeResourceRecordSets(updateRecordSetsRequest(targetDomainName));

    }


    public ChangeResourceRecordSetsResult deleteServerUrl() {

        try {
            apiGatewayBasePathMapping.deleteBasePathMappings();
            String targetDomainName = apiGatewayBasePathMapping.getTargeDomainName();
            return route53Client
                .changeResourceRecordSets(deleteRecordSetsRequest(targetDomainName));
        } catch (NotFoundException e) {
            if (log.isWarnEnabled()) {
                log.warn("Domain Name not found:" + apiGatewayBasePathMapping.getTargeDomainName());
            }

        }
        return null;
    }


    private HostedZone getHostedZone() {
        List<HostedZone> hostedZones = route53Client.listHostedZones().getHostedZones().stream()
            .filter(zone -> zone.getName().equals(zoneName))
            .collect(Collectors.toList());
        Preconditions.checkArgument(hostedZones.size() == 1,
            "There should exist exactly one hosted zone with the name " + zoneName);
        return hostedZones.get(0);

    }


    private ChangeResourceRecordSetsRequest deleteRecordSetsRequest(String serverUrl) {
        String hostZoneId = getHostedZone().getId();
        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet, ChangeAction.DELETE);
        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest()
            .withChangeBatch(new ChangeBatch().withChanges(change)).withHostedZoneId(hostZoneId);
        return request;
    }


    private ChangeResourceRecordSetsRequest updateRecordSetsRequest(String serverUrl) {
        String hostedZoneId = getHostedZone().getId();

        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet, ChangeAction.UPSERT);
        ChangeBatch changeBatch = new ChangeBatch().withChanges(change);
        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
        request.withChangeBatch(changeBatch).withHostedZoneId(hostedZoneId);
        return request;
    }

    private Change createChange(ResourceRecordSet recordSet, ChangeAction changeAction) {
        Change change = new Change();
        change.withAction(changeAction)
            .withResourceRecordSet(recordSet);
        return change;
    }

    private ResourceRecordSet createRecordSet(String serverUrl) {
        ResourceRecordSet recordSet = new ResourceRecordSet().withName(recordSetName).withType(
            RRType.CNAME)
            .withTTL(300L)
            .withResourceRecords(new ResourceRecord().withValue(serverUrl));
        return recordSet;
    }


    public void setRoute53Client(AmazonRoute53 route53Client) {
        this.route53Client = route53Client;
    }


    public ApiGatewayBasePathMapping getApiGatewayBasePathMapping() {
        return apiGatewayBasePathMapping;
    }


}
