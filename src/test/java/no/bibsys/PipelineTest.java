package no.bibsys;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.IOException;
import java.util.List;
import no.bibsys.cloudformation.ConfigurationTests;
import no.bibsys.utils.EnvUtils;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest extends ConfigurationTests implements EnvUtils {

  protected Config config= ConfigFactory.load().resolve();
  protected String projectName=config.getString("project");
  protected String branchName=config.getString("pipeline.branch");


  @Test
  @Ignore
  public void  testTemplate() throws IOException {
    Application application=new Application();

    application.run(projectName,branchName);

  }



  @Test
  @Ignore
  public void deleteStacks(){
    Application application=new Application();
    application.deleteStacks(application.pipelineStackConfiguration(projectName,branchName));
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
