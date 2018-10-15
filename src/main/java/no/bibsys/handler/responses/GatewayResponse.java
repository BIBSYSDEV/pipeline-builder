package no.bibsys.handler.responses;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * POJO containing response object for API Gateway.
 */
public class GatewayResponse {

    private  String body;
    private  Map<String, String> headers;
    private  int statusCode;



    public GatewayResponse(){};

    public GatewayResponse(final String body, final Map<String, String> headers, final int statusCode) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
    }

    public GatewayResponse(final String body){
        this.body=body;
        HashMap<String, String> map = defaultHeaders();
        this.headers=Collections.unmodifiableMap(map);
        this.statusCode=200;
    }

    public static HashMap<String, String> defaultHeaders() {
        HashMap<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json");
        return map;
    }


    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
