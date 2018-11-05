package no.bibsys.handler.requests;

import static no.bibsys.handler.requests.Action.CREATE;
import static no.bibsys.handler.requests.Action.DELETE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Preconditions;

@JsonInclude(Include.NON_NULL)
public class CustomBuildRequest extends RepositoryInfo {


    private String action;

    public CustomBuildRequest(){}

    public CustomBuildRequest(String owner, String repository, String branch,String action) {
        super(owner, repository, branch);
        this.action=action;
    }

    public String getAction() {
        Preconditions.checkNotNull(action,"\"action\" field is empty. Valid values \"create\" or \"update\"");
        Preconditions.checkArgument(validActionValue(), "Valid values: \"create\", \"update\"");
        return action;
    }


    private boolean validActionValue(){
        return action.equalsIgnoreCase(CREATE) ||
            action.equalsIgnoreCase(DELETE);
    }

    public void setAction(String action) {
        this.action = action;
    }





}
