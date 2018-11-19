package no.bibsys.lambda.deploy.handlers;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import no.bibsys.apigateway.ServerInfo;
import no.bibsys.cloudformation.Stage;
import org.junit.Test;

public class Route53UpdaterTest {


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


}
