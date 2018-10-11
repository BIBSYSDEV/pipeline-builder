package no.bibsys;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import java.io.IOException;
import java.util.List;
import no.bibsys.cloudformation.ConfigurationTests;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest extends ConfigurationTests {

  protected String projectName="dynapipe";
  protected String branchName="java-pipeline-2";
  protected String repoName="authority-registry-infrastructure";
  protected String repoOwner="BIBSYSDEV";



  @Test
  @Ignore
  public void  testTemplate() throws IOException {
    Application application=new Application();

    application.run(projectName,branchName,repoName,repoOwner,true);

  }



  @Test
  @Ignore
  public void deleteStacks() throws IOException {
    Application application=new Application();
    application.deleteStacks(application.pipelineStackConfiguration(projectName,branchName,repoName,repoOwner,true));
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
