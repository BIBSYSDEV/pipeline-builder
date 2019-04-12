package no.bibsys.aws.lambda;

public final class EnvironmentConstants {


    /**
     * Deployment Stage. (test or final).
     */
    public static final String STAGE = "STAGE";
    /**
     * Environment variable for reading the ROUTE 53 Hosted Zone name.
     */
    public static final String ZONE_NAME_ENV = "ZONE_NAME";
    /**
     * ARN of a regional certificate stored in the AWS Certificate Manager.
     */
    public static final String CERTIFICATE_ARN = "REGIONAL_CERTIFICATE_ARN";
    public static final String STACK_NAME = "STACK_NAME";
    public static final String APPLICATION_URL = "APPLICATION_URL";
    public static final String SWAGGER_API_ID = "SWAGGER_API_ID";
    public static final String SWAGGER_API_VERSION = "SWAGGER_API_VERSION";
    public static final String SWAGGER_API_OWNER = "SWAGGER_API_OWNER";
    public static final String BRANCH = "BRANCH";
    public static final String AWS_REGION = "AWS_REGION";
    public static final String REST_API_KEY_SECRET_NAME = "REST_API_KEY_SECRET_NAME";
    public static final String REST_API_KEY_SECRET_KEY = "REST_API_KEY_SECRET_KEY";
    public static final String READ_FROM_GITHUB_SECRET_NAME = "READ_FROM_GITHUB_SECRET_NAME";
    public static final String READ_FROM_GITHUB_SECRET_KEY = "READ_FROM_GITHUB_SECRET_KEY";
    public static final String GITHUB_WEBHOOK_SECRET_NAME = "GITHUB_WEBHOOK_SECRET_NAME";
    public static final String GITHUB_WEBHOOK_SECRET_KEY = "GITHUB_WEBHOOK_SECRET_KEY";
    public static final String ACCESS_SWAGGERHUB_SECRET_NAME = "ACCESS_SWAGGERHUB_SECRET_NAME";
    public static final String ACCESS_SWAGGERHUB_SECRET_KEY = "ACCESS_SWAGGERHUB_SECRET_KEY";

    private EnvironmentConstants() {
    }

}
