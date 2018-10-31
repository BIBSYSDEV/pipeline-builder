package no.bibsys.git.github;

import java.io.IOException;
import java.nio.file.Path;

public interface ResourceFileReader {

    String getBranch();

    GitInfo getGitInfo();


    String readFile(Path path) throws IOException;


}
