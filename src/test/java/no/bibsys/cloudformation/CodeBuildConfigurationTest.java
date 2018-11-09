package no.bibsys.cloudformation;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.junit.Test;

public class CodeBuildConfigurationTest extends ConfigurationTests {

    public CodeBuildConfigurationTest() throws IOException {
        super();
    }

    @Test
    public void codeBuildProjectNameShouldNotIncludeTheBranchString() {
        String buildProjecName = conf.getCodeBuildConfiguration().getBuildProjectName();
        assertThat(buildProjecName, not(containsString(branchName)));
    }


    @Test
    public void codeBuildProjectShouldContainTheNormalizedBranch() {

        String outputArtifact = conf.getCodeBuildConfiguration().getOutputArtifact();
        assertThat(outputArtifact, containsString(normalizedBranch));

    }

    @Test
    public void codeBuildProjectShouldContainTheProjectId() {
        String outputArtifact = conf.getCodeBuildConfiguration().getOutputArtifact();
        assertThat(outputArtifact, containsString(projectId));
    }

}