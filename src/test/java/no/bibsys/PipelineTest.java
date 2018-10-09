package no.bibsys;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.VersionListing;
import java.io.IOException;
import java.util.List;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import no.bibsys.utils.EnvUtils;
import no.bibsys.utils.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest implements EnvUtils {




  @Test
//  @Ignore
  public void  testTemplate() throws IOException, InterruptedException {

    Application application=new Application();

    PipelineStackConfiguration pipelineStackConfiguration =application.pipeineStackConfiguration();
    application.deleteStacks(pipelineStackConfiguration);
    CreateStackRequest stack = application
        .createStackRequest(pipelineStackConfiguration);
    AmazonCloudFormation cf= AmazonCloudFormationClientBuilder.defaultClient();

    deleteDynamoTables();

//    cf.createStack(stack);
  }

  private void deleteDynamoTables() throws InterruptedException {
    AmazonDynamoDB dynamoDB= AmazonDynamoDBClientBuilder.defaultClient();
    List<String> tableNames=dynamoDB.listTables().getTableNames();
    while (tableNames.size()>0){
    try{
      for(String table:tableNames){
        dynamoDB=AmazonDynamoDBClientBuilder.defaultClient();
        dynamoDB.deleteTable(table);
        Thread.sleep(10000);
      }
      tableNames=dynamoDB.listTables().getTableNames();
      Thread.sleep(10000);
    }
    catch(Exception e){
      e.printStackTrace();

      Thread.sleep(10000);
      dynamoDB=AmazonDynamoDBClientBuilder.defaultClient();
      tableNames=dynamoDB.listTables().getTableNames();
    }


    }
  }


}
