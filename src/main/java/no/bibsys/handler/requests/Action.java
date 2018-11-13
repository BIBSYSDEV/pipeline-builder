package no.bibsys.handler.requests;

public final class Action {


    public static final String CREATE = "create";
    public static final String DELETE = "delete";
    public static final String UPDATE_ROLE= "update-role";


    private Action(){
        throw  new IllegalStateException("Action class should not be initialized");
    }



}
