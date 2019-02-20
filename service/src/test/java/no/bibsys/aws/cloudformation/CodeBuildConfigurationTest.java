package no.bibsys.aws.cloudformation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.core.Is.is;
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
        assertThat(buildProjecName, not(containsString(BRANCH_NAME_WITH_NOT_ALLOWED_CHARS)));
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

    @Test
    public void getExecuteTestsProjectName_repositoryAndNormalizedBranch_nonNullString() {
        String projectName = conf.getCodeBuildConfiguration().getExecuteTestsProjectName();
        assertThat(projectName, is(not(emptyOrNullString())));
    }

}
