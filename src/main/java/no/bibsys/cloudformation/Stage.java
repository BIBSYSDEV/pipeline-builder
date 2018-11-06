package no.bibsys.cloudformation;


public final class Stage {


    public final static String TEST = "test";
    public final static String FINAL = "final";


    private Stage() {
        throw new IllegalStateException("Stage should not be initialized");
    }


}