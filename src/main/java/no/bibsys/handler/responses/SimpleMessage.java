package no.bibsys.handler.responses;

public class SimpleMessage {


    private String message;


    public SimpleMessage(){};

    public SimpleMessage(String message) {
        this.message = message;
    }



    public String getMessage(){
        return this.message;
    }


    public void setMessage(String message) {
        this.message = message;
    }




}
