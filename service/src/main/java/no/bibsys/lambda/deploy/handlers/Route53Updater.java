package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.apigateway.AmazonApiGateway;
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
import java.util.Optional;
import java.util.stream.Collectors;
import no.bibsys.aws.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.lambda.deploy.constants.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Route53Updater {


    /**
     * Environment variable for reading the ROUTE 53 Hosted Zone name.
     */
    public static final String ZONE_NAME_ENV = "ZONE_NAME";

    /**
     * ARN of a regional certificate stored in the AWS Certficate Manager
     */
    public static final String CERTIFICATE_ARN = "REGIONAL_CERTIFICATE_ARN";


    private static final Logger log = LoggerFactory.getLogger(Route53Updater.class);
    private final transient String zoneName;
    private final transient GitInfo gitInfo;
    private final transient String apiGatewayRestApiId;
    private final transient AmazonApiGateway apiGatewayClient;


    private final transient ApiGatewayBasePathMapping apiGatewayBasePathMapping;
    private transient final String recordSetName;


    private transient AmazonRoute53 route53Client;


    public Route53Updater(String zonName,
        GitInfo gitInfo,
        Stage stage,
        String apiGatewayRestApiId,
        AmazonApiGateway apiGatewayClient) {

        NetworkConstants networkConstants = new NetworkConstants(stage);
        this.gitInfo = gitInfo;
        this.recordSetName = networkConstants.getRecordSetName();
        this.apiGatewayClient = apiGatewayClient;
        this.zoneName = zonName;

        this.route53Client = AmazonRoute53ClientBuilder.defaultClient();
        this.apiGatewayRestApiId = apiGatewayRestApiId;

        this.apiGatewayBasePathMapping = new ApiGatewayBasePathMapping(
            apiGatewayClient,
            networkConstants.getDomainName(),
            stage);
    }


    public Route53Updater copy(Stage stage) {
        return new Route53Updater(zoneName, gitInfo, stage, apiGatewayRestApiId, apiGatewayClient);
    }


    public Optional<ChangeResourceRecordSetsResult> updateServerUrl(String certificateArn) {

        apiGatewayBasePathMapping.createBasePath(apiGatewayRestApiId, certificateArn);

        Optional<String> targetDomainName = apiGatewayBasePathMapping.getTargetDomainName();
        return targetDomainName.map(domainName ->
            route53Client.changeResourceRecordSets(updateRecordSetsRequest(domainName)));


    }


    public Optional<ChangeResourceRecordSetsResult> deleteServerUrl() {

        try {
            Optional<String> targetDomainName = apiGatewayBasePathMapping.getTargetDomainName();
            apiGatewayBasePathMapping.deleteBasePathMappings();

            return targetDomainName.map(domainName -> route53Client
                .changeResourceRecordSets(deleteRecordSetsRequest(domainName)));
        } catch (NotFoundException e) {
            if (log.isWarnEnabled()) {
                log.warn(
                    "Domain Name not found:" + apiGatewayBasePathMapping.getTargetDomainName());
            }

        }
        return Optional.empty();
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
