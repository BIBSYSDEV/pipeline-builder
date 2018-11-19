package no.bibsys.lambda.deploy.handlers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.RRType;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import no.bibsys.apigateway.ServerInfo;
import no.bibsys.cloudformation.Stage;
import no.bibsys.utils.Environment;
import no.bibsys.utils.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

public class Route53UpdaterTest {


    private final transient  String zoneName="ZoneName";

    private final transient Environment environment;
    private final transient Route53Updater route53Updater;
    private final transient AmazonRoute53 client;

    public Route53UpdaterTest() {
        environment = setupMockEnvironment();

        client=Mockito.mock(AmazonRoute53.class);
        when(client.listHostedZones()).thenReturn(new ListHostedZonesResult().withHostedZones(new HostedZone().withId("ZoneId").withName(zoneName)));

        route53Updater=new Route53Updater(environment);
        route53Updater.setClient(client);

    }

    private Environment setupIntegrationEnvironment() {
        Environment environment;
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv(Route53Updater.ZONE_NAME_ENV)).thenReturn("aws.unit.no.");
        when(environment.readEnv(Route53Updater.REPOSITORY_NAME_ENV_VAR))
            .thenReturn("authority-registry-infrastructure");
        when(environment.readEnv(Route53Updater.BRANCH_NAME_ENV_VAR))
            .thenReturn("autreg-52-update-route53-dynamically");
        when(environment.readEnv(Route53Updater.STAGE_ENV)).thenReturn("test");
        return environment;
    }


    private Environment setupMockEnvironment() {
        Environment environment;
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv(Route53Updater.ZONE_NAME_ENV)).thenReturn(zoneName);
        when(environment.readEnv(Route53Updater.REPOSITORY_NAME_ENV_VAR)).thenReturn("Repository");
        when(environment.readEnv(Route53Updater.BRANCH_NAME_ENV_VAR)).thenReturn("Branch");
        when(environment.readEnv(Route53Updater.STAGE_ENV)).thenReturn("final");
        return environment;
    }


    @Test
    public void updateRecorsrSetsRequest_ServerInfo_changeBatchWithOneChange()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method = Route53Updater.class
            .getDeclaredMethod("updateRecordSetsRequest", String.class);
        method.setAccessible(true);
        ServerInfo serverInfo = new ServerInfo("SERVER_URL", Stage.FINAL.toString());
        ChangeResourceRecordSetsRequest request = (ChangeResourceRecordSetsRequest) method
            .invoke(route53Updater, serverInfo.completeServerUrl());
        assertThat(request.getChangeBatch().getChanges().size(), is(equalTo(1)));

    }


    @Test
    public void updateRecorsrSetsRequest_ServerInfo_ChangeWithChangeActionUpsert()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method = Route53Updater.class
            .getDeclaredMethod("updateRecordSetsRequest", String.class);
        ServerInfo serverInfo = new ServerInfo("SERVERURL", Stage.FINAL.toString());
        method.setAccessible(true);
        ChangeResourceRecordSetsRequest request = (ChangeResourceRecordSetsRequest) method
            .invoke(route53Updater, serverInfo.completeServerUrl());
        Change change = request.getChangeBatch().getChanges().get(0);

        assertThat(change.getAction(), is(equalTo(ChangeAction.UPSERT.toString())));
        assertThat(change.getResourceRecordSet().getType(),is(equalTo(RRType.CNAME.toString())));
        assertThat(change.getResourceRecordSet().getName(),is(equalTo(Route53Updater.RECORD_SET_NAME)));
        assertThat(change.getResourceRecordSet().getTTL(),is(equalTo(300L)));
    }


    @Test
    @Category(IntegrationTest.class)
    public void updateRoute53() throws IOException {

        Route53Updater updater = new Route53Updater(setupIntegrationEnvironment());

        Optional<ChangeResourceRecordSetsResult> result = updater
            .updateServerUrl();

        assertThat(result, is(not(equalTo(Optional.empty()))));


    }


}
