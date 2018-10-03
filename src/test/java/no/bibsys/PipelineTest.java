package no.bibsys;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.VersionListing;
import java.io.IOException;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import org.junit.Test;

public class PipelineTest implements EnvUtils {

  private IOUtils ioUtils=new IOUtils();


  @Test
  public void  testTemplate() throws IOException, InterruptedException {
    Application application=new Application();

    PipelineStackConfiguration pipelineStackConfiguration =application.pipeineStackConfiguration();
    deleteStack(pipelineStackConfiguration);
    CreateStackRequest stack = application
        .createStackRequest(pipelineStackConfiguration);
    AmazonCloudFormation cf= AmazonCloudFormationClientBuilder.defaultClient();




    cf.createStack(stack);
  }



  private void deleteStack(PipelineStackConfiguration pipelineStackConfiguration){
    AmazonCloudFormation cf= AmazonCloudFormationClientBuilder.defaultClient();
    deleteBucket(pipelineStackConfiguration.getBucketName());

    DeleteStackRequest delete=new DeleteStackRequest().withStackName(pipelineStackConfiguration.getPipelineStackName());
    cf.deleteStack(delete);

  }

  private void deleteBucket(String bucketName){
    try {
      AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
      emptyBucket(bucketName, s3);

      s3.deleteBucket(bucketName);
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }

  private void emptyBucket(String bucketName, AmazonS3 s3) {
    VersionListing versionListing= s3.listVersions(new ListVersionsRequest().withBucketName(bucketName));
    while(versionListing.isTruncated()){
      versionListing.getVersionSummaries().forEach(version ->
          s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));
       versionListing= s3.listNextBatchOfVersions(versionListing);
    }
    versionListing.getVersionSummaries().forEach(version ->
        s3.deleteVersion(bucketName, version.getKey(), version.getVersionId()));

    ObjectListing objectListing = s3.listObjects(bucketName);
    while(objectListing.isTruncated()){
      objectListing.getObjectSummaries().stream()
          .forEach(object -> s3.deleteObject(bucketName, object.getKey()));
      objectListing=s3.listNextBatchOfObjects(objectListing);
    }

    objectListing.getObjectSummaries().stream()
        .forEach(object -> s3.deleteObject(bucketName, object.getKey()));


    if(versionListing.isTruncated() || objectListing.isTruncated() ){
        emptyBucket(bucketName,s3);
    }

  }


}
