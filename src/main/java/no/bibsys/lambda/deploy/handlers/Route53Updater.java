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
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.bibsys.apigateway.ApiGatewayApiInfo;
import no.bibsys.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.apigateway.ServerInfo;
import no.bibsys.cloudformation.CloudFormationConfigurable;
import no.bibsys.cloudformation.Stage;
import no.bibsys.lambda.deploy.constants.NetworkConstants;
import no.bibsys.utils.Environment;

public class Route53Updater {

    public static final String ZONE_NAME_ENV = "ZONE_NAME";
    public static final String REPOSITORY_NAME_ENV_VAR = "REPOSITORY";
    public static final String BRANCH_NAME_ENV_VAR = "BRANCH";
    public static final String STAGE_ENV = "STAGE";

    private final transient String zoneName;
    private final transient ApiGatewayApiInfo apiGatewayApiInfo;



    private final transient ApiGatewayBasePathMapping apiGatewayBasePathMapping;
    private transient String recordSetName;
    private transient AmazonRoute53 route53Client;


    public Route53Updater(Environment environment, AmazonApiGateway apiGatewayClient) {
        recordSetName = NetworkConstants.RECORD_SET_NAME;

        this.zoneName = environment.readEnv(ZONE_NAME_ENV);
        Stage stage = Stage.fromString(environment.readEnv(STAGE_ENV));
        if (stage.equals(Stage.TEST)) {
            recordSetName = "test." + recordSetName;
        }
        String branchName = environment.readEnv(BRANCH_NAME_ENV_VAR);
        String repository = environment.readEnv(REPOSITORY_NAME_ENV_VAR);

        this.route53Client = AmazonRoute53ClientBuilder.defaultClient();
        CloudFormationConfigurable conf = new CloudFormationConfigurable(repository, branchName);
        this.apiGatewayApiInfo = new ApiGatewayApiInfo(conf, stage.toString(), apiGatewayClient);
        this.apiGatewayBasePathMapping =  new ApiGatewayBasePathMapping(apiGatewayClient,NetworkConstants.DOMAIN_NAME, stage);
    }


    public Optional<ChangeResourceRecordSetsResult> updateServerUrl() throws IOException {
        RestApi restApi = apiGatewayApiInfo.findRestApi()
            .orElseThrow(() -> new NotFoundException("GatewayApi or GatewayApi stage not found"));

         apiGatewayBasePathMapping.createBasePath(restApi);

        String targetDomainName = apiGatewayBasePathMapping.getTargeDomainName();
        return updateRoute53CanonicalName(targetDomainName);

    }

    private Optional<ChangeResourceRecordSetsResult> updateRoute53CanonicalName(
        String targetDomainName)
        throws IOException {
        Optional<ServerInfo> serverInfo = getServerInfo();

        Optional<ChangeResourceRecordSetsRequest> updateRequest = serverInfo
            .map(info -> updateRecordSetsRequest(targetDomainName));

        Optional<ChangeResourceRecordSetsResult> result = updateRequest
            .map(r -> route53Client.changeResourceRecordSets(r));
        return result;
    }


    private Optional<ServerInfo> getServerInfo() throws IOException {
        return apiGatewayApiInfo.readServerInfo();
    }


    public HostedZone getHostedZone() {
        List<HostedZone> hostedZones = route53Client.listHostedZones().getHostedZones().stream()
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


    public void setRoute53Client(AmazonRoute53 route53Client) {
        this.route53Client = route53Client;
    }




    public ApiGatewayBasePathMapping getApiGatewayBasePathMapping() {
        return apiGatewayBasePathMapping;
    }


}
