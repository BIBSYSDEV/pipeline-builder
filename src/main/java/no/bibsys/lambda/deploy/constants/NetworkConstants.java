package no.bibsys.lambda.deploy.constants;

public final  class NetworkConstants {

    public static final String RECORD_SET_NAME = "infrastructure.entitydata.aws.unit.no.";
    //same as RECORD_SET_NAME but without the final dot.
    public static final String DOMAIN_NAME= RECORD_SET_NAME.substring(0,RECORD_SET_NAME.length()-1);


    private NetworkConstants(){
        throw new IllegalStateException("Should not be initliazed");
    }



}
