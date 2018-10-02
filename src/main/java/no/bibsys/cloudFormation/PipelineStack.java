package no.bibsys.cloudFormation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class PipelineStack extends CloudFormationConfigrable{

    private final String name;
    private final CloudFormationParameters cloudFormationParameters;

    Config config= ConfigFactory.defaultApplication();


    public PipelineStack(String projectName, String branchName) {
        super(projectName,branchName);
        this.name=pipelineStackName();
        this.cloudFormationParameters=
            new CloudFormationParameters()
    }


    public String pipelineStackName(){
        return format(projectName,"pipeline","stack");
    }


    public String getName() {
        return name;
    }





}
