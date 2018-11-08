package no.bibsys.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

/**
 * Template  for making it easier to use a POJO Lambda handler. The Amazon template RequestHandler
 * does not behave well with ApiGateway.
 *
 * <p>
 * Each class extending the HandlerTemplate should  implement the following methods:
 * </p>
 *
 *  <p>
 *      <ul>
 *      <li>Method {@code parseInput} parses an {@code InputStream} into an object of class {@code <I>}</li>.
 *      <li>Method {@code processInput} processes an {@code  <I>}  into class {@code <O>}</li>.
 *      <li>Method {@code writeOutput} writes a success message in the {@code OutputStream}</li>.
 *      <li>Method {@code writeFailure} writes a failure message in the {@code OutputStream}</li>.
 *      </ul>
 *
 *
 *  </p>
 *
 *
 * @param <I> Input class
 * @param <O> Output class
 */
public abstract class HandlerTemplate<I, O> implements RequestStreamHandler {


    private final transient Class<I> iclass;
    protected final transient ObjectMapper objectMapper = new ObjectMapper();
    protected transient LambdaLogger logger;
    protected transient OutputStream outputStream;
    private transient Context context;

    public HandlerTemplate(Class<I> iclass) {
        this.iclass = iclass;

    }


    protected void init(OutputStream outputStream, Context context) {
        this.outputStream = outputStream;
        this.context = context;
        this.logger = context.getLogger();
    }


    protected abstract I parseInput(InputStream inputStream) throws IOException;


    protected abstract O processInput(I input, Context context)
        throws IOException, URISyntaxException;

    protected abstract void writeOutput(I input,O output) throws IOException;


    protected  abstract void writeFailure(I input,Throwable exception) throws IOException;


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
            writeOutput(inputObject,response);
        } catch (Exception e) {
            logger.log(e.getMessage());
            writeFailure(inputObject,e);
        }
    }


    protected Class<I> getIClass(){
        return iclass;
    }

}
