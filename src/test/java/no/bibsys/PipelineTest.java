package no.bibsys;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import java.io.IOException;
import no.bibsys.cloudformation.PipelineStackConfiguration;
import org.junit.Test;

public class PipelineTest implements EnvUtils {

  private IOUtils ioUtils=new IOUtils();


  @Test
  public void  testTemplate() throws IOException, InterruptedException {
    Application application=new Application();

    PipelineStackConfiguration pipelineStackConfiguration =application.pipeineStackConfiguration();
    deleteStack(pipelineStackConfiguration.getName());
    CreateStackRequest stack = application
        .createStackRequest(pipelineStackConfiguration);
    AmazonCloudFormation cf= AmazonCloudFormationClientBuilder.defaultClient();




    cf.createStack(stack);
  }



  private void deleteStack(String stackName){
    AmazonCloudFormation cf= AmazonCloudFormationClientBuilder.defaultClient();
    DeleteStackRequest delete=new DeleteStackRequest().withStackName(stackName);
    cf.deleteStack(delete);

  }
















}
