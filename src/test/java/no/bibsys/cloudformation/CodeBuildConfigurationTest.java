package no.bibsys.cloudformation;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Test;

public class CodeBuildConfigurationTest extends  ConfigurationTests{

    public CodeBuildConfigurationTest() throws IOException {
        super();
    }

    @Test
    public void codeBuildProjectNameShouldNotIncludeTheBranchString() {
        String buildProjecName=conf.getCodeBuildConfiguration().getBuildProjectName();
        namingConventions(buildProjecName);
    }



    @Test
    public void getOutputArtifact() {
        String outputArtifact=conf.getCodeBuildConfiguration().getOutputArtifact();
        namingConventions(outputArtifact);


    }

    @Test
    public void getCacheBucket() {
        String bucketName=conf.getCodeBuildConfiguration().getCacheBucket();
        namingConventions(bucketName);
    }


    private void namingConventions(String buildProjecName) {
        assertThat(buildProjecName, not(containsString(branchName)));
        assertThat(buildProjecName, containsString(normalizedBranch));
        assertThat(buildProjecName, containsString(projectId));
    }
}