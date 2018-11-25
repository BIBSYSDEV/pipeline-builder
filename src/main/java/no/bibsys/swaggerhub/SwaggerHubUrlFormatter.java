package no.bibsys.swaggerhub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import no.bibsys.lambda.deploy.handlers.SwaggerHubInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SwaggerHubUrlFormatter {

    private final static Logger logger = LoggerFactory.getLogger(SwaggerHubUrlFormatter.class);
    private final static String SWAGGERHUB_RESTAPI_URL = "https://api.swaggerhub.com/apis";
    private final transient URI requestURL;


    public SwaggerHubUrlFormatter(
        SwaggerHubInfo swaggerHubInfo,
        boolean includeApiVersion,
        Map<String, String> requestParameters)
        throws URISyntaxException {

        this.requestURL = urlFormatter(apiUri(swaggerHubInfo, includeApiVersion),
            requestParameters);

    }


    public URI getRequestURL() {
        return requestURL;
    }


    private URI apiUri(SwaggerHubInfo info, boolean includeApiVersion) throws URISyntaxException {
        if (includeApiVersion) {

            URI uri = new URI(String
                .format("%s/%s/%s/%s", SWAGGERHUB_RESTAPI_URL, info.getSwaggerOrganization(),
                    info.getApiId(),
                    info.getApiVersion()));
            if (logger.isInfoEnabled()) {
                logger.info("SwaggerHub URL:{}", uri);
            }
            return uri;
        } else {
            URI uri = new URI(
                String.format("%s/%s/%s", SWAGGERHUB_RESTAPI_URL, info.getSwaggerOrganization(),
                    info.getApiId()));
            if (logger.isInfoEnabled()) {
                logger.info("SwaggerHub URL:{}", uri);
            }
            return uri;
        }

    }


    private URI urlFormatter(URI apiAddress, Map<String, String> requestParameters) {

        Optional<String> parameterOpt = joinParametersToString(requestParameters);

        //remove the last slash if there is any
        String host = apiAddress.toString().replaceAll("/$", "");
        if (parameterOpt.isPresent()) {
            String parametersString = parameterOpt.get();
            String uriString = String.join("?", host, parametersString);
            URI uri = URI.create(uriString);
            return uri;
        } else {
            return URI.create(host);
        }

    }


    private Optional<String> joinParametersToString(Map<String, String> parameters) {
        return parameters.entrySet()
            .stream()
            .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
            .reduce((str1, str2) -> String.join("&", str1, str2));
    }


}