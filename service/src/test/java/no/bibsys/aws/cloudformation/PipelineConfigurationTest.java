package no.bibsys.aws.cloudformation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

import org.junit.jupiter.api.Test;

public class PipelineConfigurationTest extends ConfigurationTests {

    @Test
    public void getInitLambdaFunctionName_projectIdAndNormalizedBranch_nonNullLambdaFunctionName() {
        assertThat(conf.getPipelineConfiguration().getInitLambdaFunctionName(),
            is(not(equalTo(null))));
        assertThat(conf.getPipelineConfiguration().getInitLambdaFunctionName(),
            containsString(conf.getNormalizedBranchName()));
        assertThat(conf.getPipelineConfiguration().getInitLambdaFunctionName(),
            containsString(conf.getProjectId()));
        assertThat(conf.getPipelineConfiguration().getInitLambdaFunctionName(),
            containsString(PipelineConfiguration.INIT_FUNCTION_SUFFIX));
    }

    @Test
    public void getDestroyLambdaFunctionName_projectIdAndNormalizedBranch_nonNullLambdaFunctionName() {
        assertThat(conf.getPipelineConfiguration().getInitLambdaFunctionName(),
            is(not(equalTo(null))));
        assertThat(conf.getPipelineConfiguration().getDestroyLambdaFunctionName(),
            containsString(conf.getNormalizedBranchName()));
        assertThat(conf.getPipelineConfiguration().getDestroyLambdaFunctionName(),
            containsString(conf.getProjectId()));
        assertThat(conf.getPipelineConfiguration().getDestroyLambdaFunctionName(),
            containsString(PipelineConfiguration.DESTROY_FUNCTION_SUFFIX));
    }

    @Test
    public void getPipelineName_projectIdAndNormalizedBranch_nonNullLambdaFunctionName() {
        assertThat(conf.getPipelineConfiguration().getPipelineName(),
            is(not(equalTo(null))));

        assertThat(conf.getPipelineConfiguration().getPipelineName(),
            containsString(conf.getNormalizedBranchName()));
        assertThat(conf.getPipelineConfiguration().getPipelineName(),
            containsString(conf.getProjectId()));
        assertThat(conf.getPipelineConfiguration().getPipelineName(),
            containsString(PipelineConfiguration.PIPELINE_NAME_SUFFIX));
    }

    @Test
    public void getCurrentServiceStackName_Stage_respectiveStack() {
        assertThat(conf.getPipelineConfiguration().getCurrentServiceStackName(Stage.TEST),
            is(equalTo(conf.getPipelineConfiguration().getTestServiceStack())));
        assertThat(conf.getPipelineConfiguration().getCurrentServiceStackName(Stage.FINAL),
            is(equalTo(conf.getPipelineConfiguration().getFinalServiceStack())));
    }

    @Test
    public void getSourceOutputArtifactName_projectIdAndNormalizedBranch_validSourceOutputname() {
        assertThat(conf.getPipelineConfiguration().getSourceOutputArtifactName(),
            is(not(equalTo(null))));
        assertThat(conf.getPipelineConfiguration().getSourceOutputArtifactName(),
            containsString(conf.getProjectId()));
        assertThat(conf.getPipelineConfiguration().getSourceOutputArtifactName(),
            containsString(conf.getNormalizedBranchName()));
        assertThat(conf.getPipelineConfiguration().getSourceOutputArtifactName(),
            containsString((PipelineConfiguration.SOURCE_OUTPUT_SUFFIX)));
    }
}
