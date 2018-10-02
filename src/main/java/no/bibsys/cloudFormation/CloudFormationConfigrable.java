package no.bibsys.cloudFormation;

import java.util.Arrays;
import java.util.List;

public class CloudFormationConfigrable {

    public static final String MASTER_BRANCH="master";

    protected final String projectName;
    protected final String branchName;

    public CloudFormationConfigrable(String projectName, String branchName) {
        this.projectName = projectName;
        this.branchName = branchName;
    }


    public String devOrProd(){
        if(branchName.equalsIgnoreCase(MASTER_BRANCH)){
            return "prod";
        }
        else{
            return "dev";
        }
    }


    public String format(String... args){
        return String.join("_",args);

    }

}
