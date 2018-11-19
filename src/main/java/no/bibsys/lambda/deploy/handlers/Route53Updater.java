package no.bibsys.lambda.deploy.handlers;

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
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.bibsys.apigateway.ApiGatewayApiInfo;
import no.bibsys.apigateway.ServerInfo;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.cloudformation.Stage;
import no.bibsys.utils.Environment;

public class Route53Updater {


    public static final String ZONE_NAME_ENV = "ZONE_NAME";
    public static final String REPOSITORY_NAME_ENV_VAR = "REPOSITORY";
    public static final String BRANCH_NAME_ENV_VAR = "BRANCH";
    public static final String STAGE_ENV = "STAGE";

    public static final String RECORD_SET_NAME = "infrastructure.entitydata.aws.unit.no.";
    private transient String recordSetName;


    private final transient String zoneName;
    private transient AmazonRoute53 client;
    private final transient ApiGatewayApiInfo apiGatewayApiInfo;


    public Route53Updater(Environment environment) {
        recordSetName = RECORD_SET_NAME;

        this.zoneName = environment.readEnv(ZONE_NAME_ENV);
        Stage stage = Stage.fromString(environment.readEnv(STAGE_ENV)).orElseThrow(() ->
            new IllegalStateException("Allowed stages:" + String.join(",", Stage.listStages())));
        if (stage.equals(Stage.TEST)) {
            recordSetName = "test." + recordSetName;
        }
        String branchName = environment.readEnv(BRANCH_NAME_ENV_VAR);
        String repository = environment.readEnv(REPOSITORY_NAME_ENV_VAR);

        this.client = AmazonRoute53ClientBuilder.defaultClient();

        CloudFormationConfigurable conf = new CloudFormationConfigurable(repository, branchName);
        this.apiGatewayApiInfo = new ApiGatewayApiInfo(conf, stage.toString());
    }


    public Route53Updater() {
        this(new Environment());
    }


    public Optional<ChangeResourceRecordSetsResult> updateServerUrl() throws IOException {
        Optional<ServerInfo> serverInfo = getServerInfo();

        Optional<ChangeResourceRecordSetsRequest> updateRequest = serverInfo
            .map(info -> updateRecordSetsRequest(info.completeServerUrl()));

        Optional<ChangeResourceRecordSetsResult> result = updateRequest
            .map(r -> client.changeResourceRecordSets(r));
        return result;


    }


    private Optional<ServerInfo> getServerInfo() throws IOException {
        return apiGatewayApiInfo.readServerInfo();
    }


    public HostedZone getHostedZone() {
        List<HostedZone> hostedZones = client.listHostedZones().getHostedZones().stream()
            .filter(zone -> zone.getName().equals(zoneName))
            .collect(Collectors.toList());
        Preconditions.checkArgument(hostedZones.size() == 1,
            "There should exist exactly one hosted zone with the name " + zoneName);
        return hostedZones.get(0);

    }


    private ChangeResourceRecordSetsRequest updateRecordSetsRequest(String serverUrl) {
        String hostedZoneId = getHostedZone().getId();

        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet);
        ChangeBatch changeBatch = new ChangeBatch().withChanges(change);
        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
        request.withChangeBatch(changeBatch).withHostedZoneId(hostedZoneId);
        return request;
    }

    private Change createChange(ResourceRecordSet recordSet) {
        Change change = new Change();
        change.withAction(ChangeAction.UPSERT)
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


    public void setClient(AmazonRoute53 client) {
        this.client = client;
    }


}
