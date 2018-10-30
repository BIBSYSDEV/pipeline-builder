package no.bibsys.handler.requests;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import no.bibsys.utils.IoUtils;
import org.junit.Test;

public class PushEventTest {


    private final transient String pushEventJson;
    private final transient PushEvent pushEvent;

    public PushEventTest() throws IOException {
        pushEventJson = IoUtils.resourceAsString(Paths.get("requests", "pushEvent.json"));
        this.pushEvent =  (PushEvent) PushEvent.create(pushEventJson).get();
    }


    @Test
    public void PushEventTestShouldReadTheBranch() {
        String branch = this.pushEvent.getBranch();
        assertThat(branch, is(equalTo("autreg-61-bugfix-lambdatrustrole")));

    }


    @Test
    public void PushEventTestShouldReadTheOwner(){
        String owner=this.pushEvent.getOwner();
        assertThat(owner,is(equalTo("BIBSYSDEV")));
    }



    @Test
    public void PushEventTestShouldReadTheRepositoryName(){
        String repository=this.pushEvent.getRepository();
        assertThat(repository,is(equalTo("authority-registry-infrastructure")));
    }


    @Test
    public void PushEventShouldAcceptOnlyPushEvents() throws IOException {
        String json=IoUtils.resourceAsString(Paths.get("requests","pullrequest.json"));
        Optional<GitEvent> event = PushEvent.create(json);
        assertThat(event.isPresent(),is(equalTo(false)));
    }


}
