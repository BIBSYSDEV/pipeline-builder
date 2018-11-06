package no.bibsys.handler.requests;

import static no.bibsys.handler.requests.Action.CREATE;
import static no.bibsys.handler.requests.Action.DELETE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Preconditions;

@JsonInclude(Include.NON_NULL)
public class CustomBuildRequest {




    private String repository;
    private String branch;
    private String owner;
    private String action;

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


    public String getOwner() {
        Preconditions.checkNotNull(owner,"\"owner\" field is empty");
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }


    public String getRepository() {
        Preconditions.checkNotNull(repository,"\"repository\" field is empty");
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getBranch() {
        Preconditions.checkNotNull(branch,"\"branch\" field is empty");
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }


}
