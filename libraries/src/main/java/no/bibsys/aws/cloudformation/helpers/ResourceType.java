package no.bibsys.aws.cloudformation.helpers;

public enum ResourceType {

    REST_API;


    public static String REST_API_RESOURCE_TYPE = "AWS::ApiGateway::RestApi";

    @Override
    public String toString() {
        if (this.equals(REST_API)) {
            return REST_API_RESOURCE_TYPE;
        } else {
            throw new IllegalStateException("Unexpected ResourceType:" + this.name());
        }


    }


}
