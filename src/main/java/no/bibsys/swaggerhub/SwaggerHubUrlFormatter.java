package no.bibsys.swaggerhub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;


public class SwaggerHubUrlFormatter {


    private final transient URI requestURL;

    /**
     *
     *
     * @param organization The SwaggerHub organization or account name
     * @param apiId The id of the api
     * @param apiVersion The version of the API documentation. Can be {@code null} if the
     *                   intended action is for the whole API
     * @param requestParameters Map of request parameters as defined by the SwaggerHub documentation
     * @throws URISyntaxException
     */
    public SwaggerHubUrlFormatter(
        String organization,
        String apiId,
        String apiVersion,
        Map<String, String> requestParameters)
        throws URISyntaxException {
        this.requestURL = urlFormatter(apiUri(organization,apiId,apiVersion),requestParameters);

    }


    public URI getRequestURL() {
        return requestURL;
    }




    private URI apiUri(String organization,String apiId,String version) throws URISyntaxException {
        if (version != null && version.length() > 0) {
            return new URI(String
                .format("https://api.swaggerhub.com/apis/%s/%s/%s", organization, apiId,
                    version));
        } else {
            return new URI(
                String.format("https://api.swaggerhub.com/apis/%s/%s", organization, apiId));
        }

    }


    private URI urlFormatter(URI apiAddress, Map<String,String> requestParameters) {

        Optional<String> parameterOpt = joinParametersToString(requestParameters);

        //remove the last slash if there is any
        String host = apiAddress.toString().replaceAll("/$", "");
        if (parameterOpt.isPresent()) {
            String parametersString=parameterOpt.get();
            String uriString = String.join("?", host, parametersString);
            URI uri=URI.create(uriString);
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