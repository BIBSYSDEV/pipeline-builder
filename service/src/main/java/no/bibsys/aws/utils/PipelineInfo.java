package no.bibsys.aws.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazonaws.services.cloudformation.AmazonCloudFormationAsync;
import com.amazonaws.services.cloudformation.AmazonCloudFormationAsyncClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.codepipeline.AWSCodePipelineAsync;
import com.amazonaws.services.codepipeline.AWSCodePipelineAsyncClientBuilder;
import com.amazonaws.services.codepipeline.model.ActionExecution;
import com.amazonaws.services.codepipeline.model.ActionState;
import com.amazonaws.services.codepipeline.model.GetPipelineStateRequest;
import com.amazonaws.services.codepipeline.model.GetPipelineStateResult;
import com.amazonaws.services.codepipeline.model.StageState;

import no.bibsys.aws.cloudformation.PipelineStackConfiguration;
import no.bibsys.aws.git.github.GithubConf;

public class PipelineInfo {

    
    public void dumpStackState(GithubConf gitInfo) {


        AmazonCloudFormationAsync cloudFormation = AmazonCloudFormationAsyncClientBuilder.defaultClient();

        PipelineStackConfiguration pipelineStackConfiguration = new PipelineStackConfiguration(gitInfo);

        String stack = pipelineStackConfiguration.getPipelineStackName();
        System.out.printf("stackName=%s repo=%s, branch=%s\n",stack, gitInfo.getRepository(), gitInfo.getBranch());
        Future<DescribeStackResourcesResult> descriptionFuture = cloudFormation.describeStackResourcesAsync(
                new DescribeStackResourcesRequest().withStackName(stack));

        while(!descriptionFuture.isDone()) {
            System.out.println("waiting for descriptionFuture...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("\nThread.sleep() was interrupted!");
                return;
            }
        }

        try {
            System.out.println("descriptionFuture.get");
            DescribeStackResourcesResult description = descriptionFuture.get();
            for (StackResource stackResource : description.getStackResources()) {
                System.out.println(stackResource.getPhysicalResourceId());
                if (stackResource.getResourceType().equalsIgnoreCase("AWS::CodePipeline::Pipeline")) {
                    dumpPipelineState(stackResource.getPhysicalResourceId());
                }
                System.out.println(stackResource.getPhysicalResourceId()+" handled...");
            }

        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e);
            return;
        }

    }

    private static void dumpPipelineState(String pipelineName) {

        GetPipelineStateRequest getPipelineStateResult = new GetPipelineStateRequest().withName(pipelineName);
        AWSCodePipelineAsync codePipeline  =  AWSCodePipelineAsyncClientBuilder.defaultClient();
        Future<GetPipelineStateResult> pipelineStateFuture = codePipeline.getPipelineStateAsync(getPipelineStateResult);


        try {

            while(!pipelineStateFuture.isDone()) {
                System.out.println("waiting for pipelineStateFuture...");
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            System.out.println(e);
            return;
        }
        try {
            String pipelineMessage = "";
            GetPipelineStateResult pipelineState = pipelineStateFuture.get();

            for (StageState stageState : pipelineState.getStageStates()) {
                // System.out.printf("stageName()=%s\n", stageState.getStageName());
                for (ActionState actionState : stageState.getActionStates()) {
                    ActionExecution latestExecution = actionState.getLatestExecution();
                    if (latestExecution != null) {
                        if (latestExecution.getStatus().equalsIgnoreCase("failed")) {
                            pipelineMessage = stageState.getStageName() + "."+ actionState.getActionName()
                            + latestExecution.getErrorDetails();
                            break;
                            // System.out.printf("\tactionName()=%s, latestExecution: %s\n", 
                            //    actionState.getActionName(), latestExecution);
                        } else {
                            pipelineMessage = stageState.getStageName() + "."+ actionState.getActionName()
                            + latestExecution;
                        }
                    }
                }
            }   

            System.out.println(pipelineMessage);
            System.out.println("Leaving dumpPipelineState");
        }  catch (InterruptedException | ExecutionException e) {
            System.err.println(e.getMessage());
            return;
        }
    }
    
    
    
    
}
