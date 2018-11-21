package no.bibsys.git.github;

import java.io.IOException;

public interface GitInfo {


     String getOwner() ;

    String getRepo() ;

    String getOauth() throws IOException;


}
