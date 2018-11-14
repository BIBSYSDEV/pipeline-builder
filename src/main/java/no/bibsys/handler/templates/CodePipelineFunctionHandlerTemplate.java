package no.bibsys.handler.templates;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.ExecutionDetails;
import com.amazonaws.services.codepipeline.model.FailureDetails;
import com.amazonaws.services.codepipeline.model.FailureType;
import com.amazonaws.services.codepipeline.model.PutJobFailureResultRequest;
import com.amazonaws.services.codepipeline.model.PutJobSuccessResultRequest;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Optional;
import no.bibsys.handler.requests.buildevents.BuildEvent;
import no.bibsys.handler.requests.buildevents.BuildEventBuilder;
import no.bibsys.handler.requests.buildevents.CodePipelineEvent;

public abstract class CodePipelineFunctionHandlerTemplate<O> extends HandlerTemplate<BuildEvent, O> {

    private final transient AWSCodePipeline pipeline = AWSCodePipelineClientBuilder.defaultClient();

    public CodePipelineFunctionHandlerTemplate() {
        super(BuildEvent.class);
    }

    @Override
    protected final BuildEvent parseInput(String inputString) throws IOException {

        return BuildEventBuilder.create(inputString);
    }


    @Override
    protected void writeOutput(BuildEvent input, O output) throws IOException {
        String outputString = objectMapper.writeValueAsString(output);

        writeOutput(outputString);
        System.out.println(input.getClass().getName());
        System.out.println(input instanceof CodePipelineEvent);

        if (isPipelineEvent(input)) {
            sendSuccessToCodePipeline((CodePipelineEvent) input,
                outputString);

        }

    }




    @Override
    protected void writeFailure(BuildEvent input, Throwable error) throws IOException {
        String outputString = Optional.ofNullable(error.getMessage())
            .orElse("Unknown error. Check stacktrace.");
        if (isPipelineEvent(input)) {
            sendFailureToCodePipeline((CodePipelineEvent) input, outputString);
        }
        writeOutput(outputString);
    }




    private void writeOutput(String outputString) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(outputString);
        }


    }
    private void sendSuccessToCodePipeline(CodePipelineEvent input, String outputString) {
        System.out.println("sending success");
        CodePipelineEvent codePipelineEvent = input;
        PutJobSuccessResultRequest success = new PutJobSuccessResultRequest();
        success.withJobId(codePipelineEvent.getId())
            .withExecutionDetails(new ExecutionDetails().withSummary(outputString));
        pipeline.putJobSuccessResult(success);
        System.out.println("sent success");
    }

    private void sendFailureToCodePipeline(CodePipelineEvent input, String outputString) {
        FailureDetails failureDetails = new FailureDetails().withMessage(outputString)
            .withType(FailureType.JobFailed);
        PutJobFailureResultRequest failure = new PutJobFailureResultRequest()
            .withJobId(input.getId()).withFailureDetails(failureDetails);
        pipeline.putJobFailureResult(failure);
    }


    private boolean isPipelineEvent(BuildEvent buildEvent) {
        return buildEvent instanceof CodePipelineEvent;

    }


}
