package no.bibsys.aws.cloudformation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class CodeBuildConfigurationTest extends ConfigurationTests {

    public CodeBuildConfigurationTest() throws IOException {
        super();
    }

    @Test
    public void initBuildProjectName_repositoryAndNormalizedBranch_buildProjectnameWithoutOriginalBranchName() {
        String buildProjecName = conf.getCodeBuildConfiguration().getBuildProjectName();
        assertThat(buildProjecName, not(containsString(branchName)));
    }


    @Test
    public void codeBuildConfiguration_repositoryAndNormalizedBranch_buildProjectnameWithNormalizedlBranchName() {

        String outputArtifact = conf.getCodeBuildConfiguration().getOutputArtifact();
        assertThat(outputArtifact, containsString(normalizedBranch));

    }

    @Test
    public void codeBuildConfiguration_repositoryAndNormalizedBranch_buildProjectnameWithProjectId() {
        String outputArtifact = conf.getCodeBuildConfiguration().getOutputArtifact();
        assertThat(outputArtifact, containsString(projectId));
    }

}
