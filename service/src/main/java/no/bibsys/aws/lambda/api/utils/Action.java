package no.bibsys.aws.lambda.api.utils;

public final class Action {


    public static final String CREATE = "create";
    public static final String DELETE = "delete";

    private Action(){
        throw  new IllegalStateException("Action class should not be initialized");
    }



}
