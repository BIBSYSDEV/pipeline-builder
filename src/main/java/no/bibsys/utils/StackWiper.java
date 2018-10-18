package no.bibsys.utils;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.VersionListing;
import java.util.List;
import java.util.stream.Collectors;
import no.bibsys.cloudformation.PipelineStackConfiguration;

public class StackWiper {


    public void deleteStacks(PipelineStackConfiguration conf) {
        AmazonCloudFormation acf = AmazonCloudFormationClientBuilder.defaultClient();

        String stack = conf.getPipelineConfiguration()
            .getTestServiceStack();

        acf.deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(acf, stack);

        stack = conf.getPipelineConfiguration().getFinalServiceStack();
        acf.deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(acf, stack);

        stack = conf.getPipelineStackName();

        acf.deleteStack(new DeleteStackRequest().withStackName(stack));
        awaitDeleteStack(acf, stack);

    }

    private void deleteBuckets(PipelineStackConfiguration pipelineStackConfiguration) {
        deleteBucket(pipelineStackConfiguration.getBucketName());
        deleteBucket(pipelineStackConfiguration.getCodeBuildConfiguration().getCacheBucket());
    }


    private void deleteLogs(PipelineStackConfiguration conf) {
        AWSLogs logsClient = AWSLogsClientBuilder.defaultClient();
        List<String> logGroups = logsClient
            .describeLogGroups().getLogGroups().stream()
            .map(group -> group.getLogGroupName())
            .filter(name -> filterLogGroups(conf, name))
            .collect(Collectors.toList());

        logGroups.stream()
            .map(group -> new DeleteLogGroupRequest().withLogGroupName(group))
            .forEach(request -> logsClient.deleteLogGroup(request));


    }


    public void wipeStacks(PipelineStackConfiguration pipelineStackConfiguration) {

        deleteBuckets(pipelineStackConfiguration);
        deleteStacks(pipelineStackConfiguration);
        deleteLogs(pipelineStackConfiguration);
    }


    private boolean filterLogGroups(PipelineStackConfiguration conf, String name) {
        boolean result = name.contains(conf.getProjectId())
            &&  name.contains(conf.getShortBranch());
        return result;
    }


    private void awaitDeleteStack(AmazonCloudFormation acf, String stackname) {

        int counter = 0;
        List<String> stackNames = acf.describeStacks().getStacks().stream()
            .map(stack -> stack.getStackName())
            .collect(Collectors.toList());

        while (stackNames.contains(stackname) && counter < 100) {
            stackNames = acf.describeStacks().getStacks().stream()
                .map(stack -> stack.getStackName())
                .collect(Collectors.toList());
            counter++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }

    }


    private void deleteBucket(String bucketName) {
        try {
            AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
            emptyBucket(bucketName, s3);
            s3.deleteBucket(bucketName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void emptyBucket(String bucketName, AmazonS3 s3) {
        VersionListing versionListing = s3
            .listVersions(new ListVersionsRequest().withBucketName(bucketName));

        while (versionListing.isTruncated()) {
            deleteVersionBatch(bucketName, s3, versionListing);
            versionListing = s3.listNextBatchOfVersions(versionListing);
        }
        deleteVersionBatch(bucketName, s3, versionListing);

        ObjectListing objectListing = s3.listObjects(bucketName);
        while (objectListing.isTruncated()) {
            deleteObjectBatch(bucketName, s3, objectListing);
            objectListing = s3.listNextBatchOfObjects(objectListing);
        }

        deleteObjectBatch(bucketName, s3, objectListing);

        // Be sure we have nothing more to delete
        if (!versionListing.getVersionSummaries().isEmpty()
            || !objectListing.getObjectSummaries().isEmpty()) {
            emptyBucket(bucketName, s3);
        }

    }


    private void deleteObjectBatch(String bucketName, AmazonS3 s3, ObjectListing objectListing) {
        objectListing.getObjectSummaries().stream()
            .forEach(object -> s3.deleteObject(bucketName, object.getKey()));
    }

    private void deleteVersionBatch(String bucketName, AmazonS3 s3, VersionListing versionListing) {
        versionListing.getVersionSummaries().forEach(version ->
            s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));
    }


}
