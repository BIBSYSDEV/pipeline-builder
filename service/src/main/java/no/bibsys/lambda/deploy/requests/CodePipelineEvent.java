package no.bibsys.lambda.deploy.requests;

public class CodePipelineEvent implements DeployEvent {


    private final String id;

    public CodePipelineEvent(String id) {
        this.id = id;
    }


    public String getId() {
        return id;
    }


}


