package no.bibsys.aws.lambda.api.requests;

import static no.bibsys.aws.lambda.api.utils.Action.CREATE;
import static no.bibsys.aws.lambda.api.utils.Action.DELETE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Preconditions;

@JsonInclude(Include.NON_NULL)
public final class UpdateStackRequest extends GitEvent {

    private static final String ERROR_MESSAGE = "\"action\" field is empty. Valid values \"create\" or \"delete\"";
    private static final String INVALID_ACTION_ERROR_MESSAGE = "Valid values: \"create\", \"delete\"";
    private String action;

    public UpdateStackRequest() {
        super();
    }

    public UpdateStackRequest(String owner, String repository, String branch, String action) {
        super(owner, repository, branch);
        this.action = action;
    }

    public String getAction() {
        Preconditions.checkNotNull(action, ERROR_MESSAGE);
        Preconditions.checkArgument(validActionValue(), INVALID_ACTION_ERROR_MESSAGE);
        return action;
    }


    private boolean validActionValue() {
        return action.equalsIgnoreCase(CREATE) || action.equalsIgnoreCase(DELETE);
    }

    public void setAction(String action) {
        this.action = action;
    }



}
