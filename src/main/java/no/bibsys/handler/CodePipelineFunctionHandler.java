package no.bibsys.handler;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.FailureDetails;
import com.amazonaws.services.codepipeline.model.PutJobFailureResultRequest;
import com.amazonaws.services.codepipeline.model.PutJobSuccessResultRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;
import no.bibsys.handler.requests.CodePipelineEvent;
import no.bibsys.utils.IoUtils;

public abstract class CodePipelineFunctionHandler<O> extends HandlerTemplate<CodePipelineEvent,O> {

    private final transient AWSCodePipeline pipeline=AWSCodePipelineClientBuilder.defaultClient();

    public CodePipelineFunctionHandler() {
        super(CodePipelineEvent.class);
    }

    @Override
    protected final CodePipelineEvent parseInput(InputStream inputStream) throws IOException {
        String jsonSting= IoUtils.streamToString(inputStream);
        System.out.println(jsonSting);
        return CodePipelineEvent.create(jsonSting);
    }


    @Override
    protected void writeOutput(CodePipelineEvent input,O output) throws IOException {
        String outputString = objectMapper.writeValueAsString(output);
        PutJobSuccessResultRequest success = new PutJobSuccessResultRequest();
        String continutationToken=createContinuationToken(input);
        success.withJobId(input.getId()).withContinuationToken(continutationToken);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(outputString);
        writer.close();
        pipeline.putJobSuccessResult(success);

    }



    @Override
    protected void writeFailure(CodePipelineEvent input,Throwable error) throws IOException {
        String outputString = Optional.ofNullable(error.getMessage())
            .orElse("Unknown error. Check stacktrace.");

        FailureDetails failureDetails=new FailureDetails().withMessage(outputString);
        PutJobFailureResultRequest failure=new PutJobFailureResultRequest()
            .withJobId(input.getId()).withFailureDetails(failureDetails);
        pipeline.putJobFailureResult(failure);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(outputString);
        writer.close();
    }

    private String createContinuationToken(CodePipelineEvent input) throws JsonProcessingException {
        ObjectNode continuationToken = objectMapper.getNodeFactory().objectNode();
        continuationToken.put("previous_job_id",input.getId());
        return objectMapper.writeValueAsString(continuationToken);

    }





}
