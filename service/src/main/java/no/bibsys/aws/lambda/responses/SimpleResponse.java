package no.bibsys.aws.lambda.responses;

public class SimpleResponse {


    private String message;


    public SimpleResponse() {}



    public SimpleResponse(String message) {
        this.message = message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
