package no.bibsys.handler.templates;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Optional;
import no.bibsys.handler.responses.GatewayResponse;
import no.bibsys.utils.ApiMessageParser;
import org.apache.http.HttpStatus;

public abstract class ApiGatewayHandlerTemplate<I, O> extends HandlerTemplate<I, O> {


    private final transient ApiMessageParser<I> inputParser = new ApiMessageParser<>();


    public ApiGatewayHandlerTemplate(Class<I> iclass) {
        super(iclass);
    }


    @Override
    protected I parseInput(String inputString)
        throws IOException {
        I input = inputParser.getBodyElementFromJson(inputString, getIClass());
        return input;

    }


    @Override
    protected final O processInput(I input, String apiGatewayInputString, Context context)
        throws IOException {
        Map<String, String> headers = inputParser.getHeadersFromJson(apiGatewayInputString);
        return processInput(input, headers, context);
    }


    protected abstract O processInput(I input, Map<String, String> headers, Context context)
        throws IOException;




    @Override
    protected void writeOutput(I input, O output) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String outputString = objectMapper.writeValueAsString(output);
            GatewayResponse gatewayResponse = new GatewayResponse(outputString);
            String responseJson = objectMapper.writeValueAsString(gatewayResponse);
            writer.write(responseJson);
        }


    }


    @Override
    protected void writeFailure(I input, Throwable error) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

            String outputString = Optional.ofNullable(error.getMessage())
                .orElse("Unknown error. Check stacktrace.");

            GatewayResponse gatewayResponse = new GatewayResponse(outputString,
                GatewayResponse.defaultHeaders(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
            gatewayResponse.setBody(outputString);
            String gateWayResponseJson = objectMapper.writeValueAsString(gatewayResponse);
            writer.write(gateWayResponseJson);
        }

    }


}
