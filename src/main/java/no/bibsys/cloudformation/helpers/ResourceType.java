package no.bibsys.cloudformation.helpers;

public enum ResourceType {

    REST_API;


    @Override
    public String toString() {

        switch (this) {
            case REST_API:
                return "AWS::ApiGateway::RestApi";

        }

    }


    @Override
    public String name() {
        return toString();

    }


}
