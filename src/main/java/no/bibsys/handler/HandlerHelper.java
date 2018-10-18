package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import no.bibsys.Application;
import no.bibsys.handler.responses.GatewayResponse;
import no.bibsys.utils.ApiMessageParser;
import no.bibsys.utils.Environment;
import no.bibsys.utils.IoUtils;
import org.apache.http.HttpStatus;


public abstract class HandlerHelper<I, O> implements RequestStreamHandler {


    private final transient Class<I> iclass;
    private final transient ApiMessageParser<I> inputParser = new ApiMessageParser<>();
    private final transient IoUtils ioUtils = new IoUtils();
    private final transient ObjectMapper objectMapper = new ObjectMapper();
    protected transient LambdaLogger logger;
    private transient OutputStream outputStream;
    private transient Context context;

    public HandlerHelper(Class<I> iclass) {
        this.iclass = iclass;

    }


    private void init(OutputStream outputStream, Context context) {
        this.outputStream = outputStream;
        this.context = context;
        this.logger = context.getLogger();
    }

    public I parseInput(InputStream inputStream)
        throws IOException {
        String inputString = ioUtils.streamToString(inputStream);
        I input = inputParser.getBodyElementFromJson(inputString, iclass);
        return input;

    }

    protected abstract O processInput(I input, Context context) throws IOException;

    public void writeOutput(O output) throws IOException {
        String outputString = objectMapper.writeValueAsString(output);
        GatewayResponse gatewayResponse = new GatewayResponse(outputString);
        String responseJson = objectMapper.writeValueAsString(gatewayResponse);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(responseJson);
        writer.close();

    }


    public void writerFailure(Throwable error) throws IOException {
        String outputString = error.getMessage();
        GatewayResponse gatewayResponse = new GatewayResponse(outputString,
            GatewayResponse.defaultHeaders(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        String responseJson = objectMapper.writeValueAsString(gatewayResponse);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(responseJson);
        writer.close();
    }


    protected Context getContext() {
        return this.context;
    }


    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context)
        throws IOException {
        init(output, context);
        I inputObject = parseInput(input);
        O response = null;
        try {
            response = processInput(inputObject, context);
            writeOutput(response);
        } catch (IOException e) {
            logger.log(e.getMessage());
            writerFailure(e);
        }
    }



    protected void deleteStacks(String repoOwner,String repo,String branch, Environment env)
        throws IOException {
        Application application = new Application(env);
        application
            .withRepoOwner(repoOwner)
            .withRepoName(repo)
            .withBranch(branch)

            .wipeStacks();
    }

    protected void createStacks(String repoOwner,String repo,String branch, Environment env)
        throws IOException {

        Application application = new Application(env);
        application
            .withRepoOwner(repoOwner)
            .withRepoName(repo)
            .withBranch(branch)
            .createStacks();
    }



}
