package no.bibsys.handler.templates;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
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
        if (error instanceof UnauthorizedException) {
            unauthorizedFailure(input, (UnauthorizedException) error);
        } else {
            unknownError(input, error);
        }

    }


    protected void writeFailure(I input, Throwable error, int statusCode, String message)
        throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String outputString = Optional.ofNullable(error.getMessage())
                .orElse(message);
            GatewayResponse gatewayResponse = new GatewayResponse(outputString,
                GatewayResponse.defaultHeaders(), statusCode);
            gatewayResponse.setBody(outputString);
            String gateWayResponseJson = objectMapper.writeValueAsString(gatewayResponse);
            writer.write(gateWayResponseJson);
        }

    }


    private void unknownError(I input, Throwable error) throws IOException {
        writeFailure(input, error, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unknown error.Check logs");

    }


    protected void unauthorizedFailure(I input, UnauthorizedException unauthorizedException)
        throws IOException {
        writeFailure(input, unauthorizedException, HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
    }


}
