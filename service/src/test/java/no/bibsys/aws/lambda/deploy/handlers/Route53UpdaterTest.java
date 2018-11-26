package no.bibsys.aws.lambda.deploy.handlers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.RRType;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import no.bibsys.aws.apigateway.ServerInfo;
import no.bibsys.aws.cloudformation.PipelineConfiguration;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.cloudformation.helpers.ResourceType;
import no.bibsys.aws.cloudformation.helpers.StackResources;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.git.github.GitInfoImpl;
import no.bibsys.aws.route53.Route53Updater;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.utils.network.NetworkConstants;
import no.bibsys.utils.IntegrationTest;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

public class Route53UpdaterTest {


    private final transient String zoneName = "ZoneName";

    private final transient Route53Updater route53Updater;

    private final transient AmazonApiGateway apiGateway = Mockito.mock(AmazonApiGateway.class);

    public Route53UpdaterTest() {

        AmazonRoute53 client = Mockito.mock(AmazonRoute53.class);
        when(client.listHostedZones()).thenReturn(new ListHostedZonesResult()
            .withHostedZones(new HostedZone().withId("ZoneId").withName(zoneName)));
        GitInfo gitInfo = new GitInfoImpl("owner", "repository", "branch");
        StaticUrlInfo staticUrlINfo = StaticUrlInfo
            .create(Stage.TEST, zoneName, "some.url.goes.here.");
        route53Updater = new Route53Updater(staticUrlINfo, gitInfo, Stage.TEST,
            "apiGatewarRestApiId",
            apiGateway);
        route53Updater.setRoute53Client(client);

    }


    @Test
    public void updateRecorsrSetsRequest_ServerInfo_changeBatchWithOneChange()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method = Route53Updater.class
            .getDeclaredMethod("updateRecordSetsRequest", String.class);
        method.setAccessible(true);
        ServerInfo serverInfo = new ServerInfo("SERVER_URL", Stage.FINAL.toString());
        ChangeResourceRecordSetsRequest request = (ChangeResourceRecordSetsRequest) method
            .invoke(route53Updater, serverInfo.serverAddress());
        assertThat(request.getChangeBatch().getChanges().size(), is(equalTo(1)));

    }


    @Test
    public void updateRecorsrSetsRequest_ServerInfo_ChangeWithChangeActionUpsert()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method = Route53Updater.class
            .getDeclaredMethod("updateRecordSetsRequest", String.class);
        ServerInfo serverInfo = new ServerInfo("SERVERURL", Stage.TEST.toString());
        method.setAccessible(true);
        ChangeResourceRecordSetsRequest request = route53Updater
            .updateRecordSetsRequest(serverInfo.getServerUrl());

        Change change = request.getChangeBatch().getChanges().get(0);

        assertThat(change.getAction(), is(equalTo(ChangeAction.UPSERT.toString())));
        assertThat(change.getResourceRecordSet().getType(), is(equalTo(RRType.CNAME.toString())));
        assertThat(change.getResourceRecordSet().getName(), CoreMatchers.startsWith("test."));
        assertThat(change.getResourceRecordSet().getName(),
            not(CoreMatchers.startsWith("test.test.")));
        assertThat(change.getResourceRecordSet().getTTL(), is(equalTo(300L)));
    }


    @Test
    @Category(IntegrationTest.class)
    public void updateRoute53() throws IOException {

        String zoneName = "aws.unit.no.";
        String repository = "authority-registry-infrastructure";
        String branch = extractLocalBranch();
        String certificateArn = "arn:aws:acm:eu-west-1:933878624978:certificate/b163e7df-2e12-4abf-ae91-7a8bbd19fb9a";

        GitInfoImpl gitInfo = new GitInfoImpl(null, repository, branch);

        PipelineConfiguration pipelineConfiguration = new PipelineConfiguration(repository, branch);
        StackResources stackResources = new StackResources(
            pipelineConfiguration.getTestServiceStack());
        String restApiId = stackResources.getResourceIds(ResourceType.REST_API).
            stream().findAny().orElseThrow(() -> new NotFoundException("Could not find a RestAPi"));
        StaticUrlInfo staticUrlINfo = StaticUrlInfo
            .create(Stage.TEST, zoneName, NetworkConstants.RECORD_SET_NAME);
        Route53Updater updater = new Route53Updater(staticUrlINfo, gitInfo, Stage.TEST, restApiId,
            AmazonApiGatewayClientBuilder.defaultClient());

        Optional<ChangeResourceRecordSetsResult> result = updater.updateServerUrl(certificateArn);

        assertThat(result, is(not(equalTo(Optional.empty()))));

    }

    private String extractLocalBranch() throws IOException {
        Repository repo = FileRepositoryBuilder.create(new File(".", ".git"));
        return repo.getBranch();
    }


}
