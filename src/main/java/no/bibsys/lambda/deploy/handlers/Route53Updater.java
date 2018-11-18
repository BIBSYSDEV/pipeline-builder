package no.bibsys.lambda.deploy.handlers;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
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
import no.bibsys.apigateway.ApiGatewayApiInfo.ServerInfo;
import no.bibsys.apigateway.ServerInfo;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.cloudformation.Stage;
import no.bibsys.utils.Environment;

public abstract class Route53Updater {


    private static String ZONE_NAME_ENV = "ZONE_NAME";
    private static String RECORD_SET_NAME = "infrastructure.entitydata.aws.unit.no.";
    private static String REPOSITORY_NAME = "REPOSITORY";
    private static String BRANCH_NAME = "BRANCH";


    private final transient String zoneName;
    private transient AmazonRoute53 client;
    private final transient ApiGatewayApiInfo apiGatewayApiInfo;


    public Route53Updater(Environment environment) {

        this.zoneName = environment.readEnv(ZONE_NAME_ENV);
        String branchName = environment.readEnv(BRANCH_NAME);
        String repository = environment.readEnv(REPOSITORY_NAME);
        this.client = AmazonRoute53ClientBuilder.defaultClient();

        CloudFormationConfigurable conf = new CloudFormationConfigurable(repository, branchName);
        this.apiGatewayApiInfo = new ApiGatewayApiInfo(conf, Stage.FINAL.toString());
    }


    public Route53Updater() {
        this(new Environment());
    }


    private Optional<ServerInfo> getServerInfo() throws IOException {
        return apiGatewayApiInfo.readServerInfo();
    }


    public HostedZone getHostedZone() {
        List<HostedZone> hostedZones = client.listHostedZones().getHostedZones().stream()
            .filter(zone -> zone.getName().equals(zoneName))
            .collect(Collectors.toList());
        Preconditions.checkArgument(hostedZones.size() <= 1, "More than "
            + "one hosted zones found with the name " + zoneName);
        Preconditions.checkArgument(hostedZones.size() > 0, "No "
            + "hosted zones found with the name " + zoneName);

        return hostedZones.get(0);

    }


    private void updateServerUrl(ServerInfo serverInfo) {
        String hostedZoneId = getHostedZone().getName();

        ResourceRecordSet recordSet = createRecordSet(serverInfo);
        Change change = createChange(recordSet);
        ChangeBatch changeBatch = new ChangeBatch().withChanges(change);
        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
        request.withHostedZoneId(hostedZoneId).withChangeBatch(changeBatch);


    }

    private Change createChange(ResourceRecordSet recordSet) {
        Change change = new Change();
        change.withAction(ChangeAction.UPSERT)
            .withResourceRecordSet(recordSet);
        return change;
    }

    private ResourceRecordSet createRecordSet(ServerInfo serverInfo) {
        ResourceRecordSet recordSet = new ResourceRecordSet().withName(RECORD_SET_NAME).withType(
            RRType.CNAME)
            .withTTL(300L)
            .withResourceRecords(new ResourceRecord().withValue(serverInfo.getServerUrl()));
        return recordSet;
    }


    public void setClient(AmazonRoute53 client) {
        this.client = client;
    }


}
