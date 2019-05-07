package no.bibsys.aws.lambda.api.utils;

import java.util.Locale;

public enum Action {

    CREATE, DELETE, INFO;

    private static final String CREATE_STRING = "create";
    private static final String DELETE_STRING = "delete";
    private static final String INFO_STRING = "info";

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.getDefault());
    }

    public static Action fromString(String actionString) {
        if (actionString.equalsIgnoreCase(CREATE_STRING)) {
            return CREATE;
        } else if (actionString.equalsIgnoreCase(DELETE_STRING)) {
            return DELETE;
        } else if (actionString.equalsIgnoreCase(INFO_STRING)) {
            return INFO;
        } else {
            throw new IllegalArgumentException();
        }
    }


}
