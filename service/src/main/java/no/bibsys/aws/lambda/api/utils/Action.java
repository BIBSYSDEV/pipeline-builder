package no.bibsys.aws.lambda.api.utils;

import java.util.Locale;

public enum Action {

    CREATE, DELETE;

    private static final String CREATE_STRING = "create";
    private static final String DELETE_STRING = "delete";

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.getDefault());
    }

    public static Action fromString(String actionString) {
        if (actionString.equalsIgnoreCase(CREATE_STRING)) {
            return CREATE;
        } else if (actionString.equalsIgnoreCase(DELETE_STRING)) {
            return DELETE;
        } else {
            throw new IllegalArgumentException();
        }
    }


}
