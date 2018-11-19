package no.bibsys.lambda.deploy.handlers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import no.bibsys.apigateway.ServerInfo;
import no.bibsys.cloudformation.Stage;
import no.bibsys.utils.Environment;
import no.bibsys.utils.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class Route53UpdaterTest {


    private final transient Environment environment;

    public Route53UpdaterTest() {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        });
    }


    @Test
    public void updateRecorsrSetsRequest_ServerInfo_ChangeBatchWithOneChange()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Route53Updater route53Updater = new Route53Updater();
        Method method = Route53Updater.class
            .getDeclaredMethod("updateRecordSetsRequest", ServerInfo.class);
        ServerInfo serverInfo = new ServerInfo("SERVERURL", Stage.FINAL.toString());
        ChangeResourceRecordSetsRequest request = (ChangeResourceRecordSetsRequest) method
            .invoke(route53Updater, serverInfo);
        assertThat(request.getChangeBatch().getChanges().size(), is(equalTo(1)));

    }


    @Test
    public void updateRecorsrSetsRequest_ServerInfo_ChangeWithChangeActionUpsert()
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Route53Updater route53Updater = new Route53Updater();
        Method method = Route53Updater.class
            .getDeclaredMethod("updateRecordSetsRequest", ServerInfo.class);
        ServerInfo serverInfo = new ServerInfo("SERVERURL", Stage.FINAL.toString());
        ChangeResourceRecordSetsRequest request = (ChangeResourceRecordSetsRequest) method
            .invoke(route53Updater, serverInfo);
        Change change = request.getChangeBatch().getChanges().get(0);

        assertThat(change.getAction(), is(equalTo(ChangeAction.UPSERT.toString())));
    }


    @Test
    @Category(IntegrationTest.class)
    public void updateRoute53() {
        Route53Updater updater = new Route53Updater();

    }


}
