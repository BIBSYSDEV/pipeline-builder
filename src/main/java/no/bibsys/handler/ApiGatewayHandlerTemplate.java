package no.bibsys.handler;

import java.io.IOException;
import java.io.InputStream;
import no.bibsys.utils.ApiMessageParser;
import no.bibsys.utils.IoUtils;

public abstract class ApiGatewayHandlerTemplate<I,O> extends HandlerTemplate<I,O> {


    private final transient ApiMessageParser<I> inputParser = new ApiMessageParser<>();


    public ApiGatewayHandlerTemplate(Class<I> iclass) {
        super(iclass);
    }


    @Override
    protected I parseInput(InputStream inputStream)
        throws IOException {
        String inputString = IoUtils.streamToString(inputStream);
        I input = inputParser.getBodyElementFromJson(inputString, getIClass());
        return input;

    }
}
