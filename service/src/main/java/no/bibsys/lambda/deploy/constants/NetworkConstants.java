package no.bibsys.lambda.deploy.constants;

import no.bibsys.aws.cloudformation.Stage;

public final  class NetworkConstants {

    private static final String RECORD_SET_NAME = "infrastructure.entitydata.aws.unit.no.";
    //same as RECORD_SET_NAME but without the final dot.
    private static final String DOMAIN_NAME= RECORD_SET_NAME.substring(0,RECORD_SET_NAME.length()-1);


    private final transient Stage stage;
    public NetworkConstants(Stage stage){
        this.stage=stage;
    }


    public String  getDomainName(){
        if(stage.equals(Stage.FINAL)){
            return DOMAIN_NAME;
        }
        else{
            return "test."+DOMAIN_NAME;
        }
    }

    public String  getRecordSetName(){
        if(stage.equals(Stage.FINAL)){
            return RECORD_SET_NAME;
        }
        else{
            return "test."+RECORD_SET_NAME;
        }
    }



}
