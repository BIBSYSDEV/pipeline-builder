package no.bibsys.aws.lambda;

public class EnvironmentConstants {


    /**
     * Deployment Stage. (test or final).
     */
    public static final String STAGE="STAGE";


    /**
     * Environment variable for reading the ROUTE 53 Hosted Zone name.
     */
    public static final String ZONE_NAME_ENV = "ZONE_NAME";

    /**
     * ARN of a regional certificate stored in the AWS Certificate Manager.
     */
    public static final String CERTIFICATE_ARN = "REGIONAL_CERTIFICATE_ARN";


    public static final String STACK_NAME ="STACK_NAME";


    public static final String APPLICATION_URL="APPLICATION_URL";


    public static final String SWAGGER_API_ID="SWAGGER_API_ID";

    public static final String SWAGGER_API_VERSION="SWAGGER_API_VERSION";

    public static final String SWAGGER_API_OWNER="SWAGGER_API_OWNER";


    public static final String BRANCH = "BRANCH";
}
