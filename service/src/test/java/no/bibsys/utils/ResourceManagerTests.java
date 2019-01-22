package no.bibsys.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThan;

import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.utils.resources.ResourceManager;
import org.junit.jupiter.api.Test;

public class ResourceManagerTests extends ResourceManager {


    @Test
    public void initRoute53Updater_gitBranch_alteredUrlIfBranchNotMaster() {
        String originalUrl = "originalUrl";
        StaticUrlInfo staticUrlInfo = new StaticUrlInfo("zoneName", originalUrl, Stage.FINAL);

        StaticUrlInfo info = initStaticUrlInfo(staticUrlInfo, "notmaster");

        String newUrl = info.getRecordSetName();

        int prefixIndex = newUrl.indexOf(".");
        assertThat(prefixIndex, is(greaterThan(0)));
        assertThat(newUrl, is(not(equalTo(originalUrl))));

    }


    @Test
    public void initRoute53Updater_gitBranch_originaldUrlIfBrancIsMaster() {
        String originalUrl = "originalUrl";
        StaticUrlInfo staticUrlInfo = new StaticUrlInfo("zoneName", originalUrl, Stage.FINAL);

        StaticUrlInfo info = initStaticUrlInfo(staticUrlInfo, "master");

        String newUrl = info.getRecordSetName();

        int prefixIndex = newUrl.indexOf(".");
        assertThat(prefixIndex, is(lessThan(0)));
        assertThat(newUrl, is(equalTo(originalUrl)));

    }

}
