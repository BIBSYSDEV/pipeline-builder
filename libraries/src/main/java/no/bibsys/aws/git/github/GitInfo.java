package no.bibsys.aws.git.github;

import java.io.IOException;

public interface GitInfo {


     String getOwner() ;

    String getRepository();

    String getOauth() throws IOException;

    String getBranch();


}
