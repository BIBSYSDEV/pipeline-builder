package no.bibsys;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import java.io.IOException;
import java.util.List;
import no.bibsys.utils.EnvUtils;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest implements EnvUtils {


  @Test
  @Ignore
  public void  testTemplate() throws IOException, InterruptedException {

    Application application=new Application();
    application.run();

  }



  @Test
  @Ignore
  public void deleteStacks(){
    Application application=new Application();
    application.deleteStacks(application.pipelineStackConfiguration());
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
