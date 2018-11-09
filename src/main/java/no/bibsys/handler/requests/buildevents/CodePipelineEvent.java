package no.bibsys.handler.requests.buildevents;

public class CodePipelineEvent  implements BuildEvent{


    private final String id;

    public CodePipelineEvent(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }


}


