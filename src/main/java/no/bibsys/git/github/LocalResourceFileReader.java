package no.bibsys.git.github;

import java.io.IOException;
import java.nio.file.Path;
import no.bibsys.utils.IoUtils;

public class LocalResourceFileReader implements ResourceFileReader {


    private final String branch;
    private final GitInfo gitInfo;

    public LocalResourceFileReader(GitInfo gitInfo, String branch){
        this.branch=branch;
        this.gitInfo=gitInfo;
    }



    @Override
    public String getBranch() {
        return branch;
    }

    @Override
    public GitInfo getGitInfo() {
        return gitInfo;
    }

    @Override
    public String readFile(Path path) throws IOException {
        return IoUtils.fileAsString(path);

    }
}
