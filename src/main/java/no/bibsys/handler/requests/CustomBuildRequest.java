package no.bibsys.handler.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Preconditions;

@JsonInclude(Include.NON_NULL)
public class CustomBuildRequest {

    public static final String CREATE = "create";
    public static final String DELETE = "delete";

    private String repositoryName;
    private String branch;
    private String owner;
    private String action;

    public String getAction() {
        Preconditions.checkNotNull(action,"\"action\" field is empty. Valid values \"create\" or \"update\"");
        Preconditions.checkArgument(validActionValue(), "Valid values: \"create\", \"update\"");
        return action;
    }


    private boolean validActionValue(){
        return action.trim().toLowerCase().equals(CREATE) ||
            action.trim().toLowerCase().equals(DELETE);
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

    public String getRepositoryName() {
        Preconditions.checkNotNull(repositoryName,"\"repositoryName\" field is empty");
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getBranch() {
        Preconditions.checkNotNull(branch,"\"branch\" field is empty");
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }


}
