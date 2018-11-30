package no.bibsys.utils;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import no.bibsys.aws.Application;
import no.bibsys.aws.git.github.GithubConf;
import org.junit.Ignore;
import org.junit.Test;

public class PipelineTest {


    private String branchName = "autreg-88-static-url";
    private String repoName = "authority-registry";
    private String repoOwner = "BIBSYSDEV";


    @Test
    @Ignore
    public void createStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.createStacks();
    }

    @Test
    @Ignore
    public void deleteStacks() throws IOException, URISyntaxException {
        Application application = initApplication();
        application.wipeStacks();

    }


    private Application initApplication() throws IOException {
        GithubConf githubConf = new GithubConf(repoOwner, repoName, branchName);
        return new Application(githubConf);
    }


    @Test
    @Ignore
    public void foo() {
        AWSLogs logsClient = AWSLogsClientBuilder.defaultClient();
        List<String> logGroups =
            logsClient.describeLogGroups().getLogGroups().stream()
                .map(group -> group.getLogGroupName())
                .collect(Collectors.toList());

        logGroups.stream().map(group -> new DeleteLogGroupRequest().withLogGroupName(group))
            .forEach(request -> logsClient.deleteLogGroup(request));

    }


}
