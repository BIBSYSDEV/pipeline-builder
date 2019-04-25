package no.bibsys.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThan;

import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.route53.StaticUrlInfo;
import no.bibsys.aws.testtutils.LocalStackTest;
import no.bibsys.aws.utils.resources.ResourceManager;
import org.junit.jupiter.api.Test;

public class ResourceManagerTest extends ResourceManager {

    private static final String ORIGINAL_URL = "original.Url.";
    private static final String ZONE_NAME = "zone.Name.";
    private static final String ARBITRARY_BRANCH = "notmaster";
    private static final String MASTER_BRANCH = "master";

    public ResourceManagerTest() {
        super(new LocalStackTest().mockCloudFormationWithStack());
    }

    @Test
    public void initRoute53Updater_gitBranch_alteredUrlIfBranchNotMaster() {

        StaticUrlInfo staticUrlInfo = new StaticUrlInfo(ZONE_NAME, ORIGINAL_URL, Stage.FINAL);
        StaticUrlInfo info = initStaticUrlInfo(staticUrlInfo, ARBITRARY_BRANCH);
        String newUrl = info.getRecordSetName();

        int prefixIndex = newUrl.indexOf(".");
        assertThat(prefixIndex, is(greaterThan(0)));
        assertThat(newUrl, is(not(equalTo(ORIGINAL_URL))));

    }


    @Test
    public void initRoute53Updater_gitBranch_originaldUrlIfBrancIsMaster() {

        StaticUrlInfo staticUrlInfo = new StaticUrlInfo(ZONE_NAME, ORIGINAL_URL, Stage.FINAL);
        StaticUrlInfo info = initStaticUrlInfo(staticUrlInfo, MASTER_BRANCH);
        String newUrl = info.getRecordSetName();
        assertThat(newUrl, is(equalTo(ORIGINAL_URL)));
    }

    @Test
    public void initStaticUrlInfo_noMasterBranchTestStage_testAndRandomPrefix() {
        StaticUrlInfo staticUrlInfo = new StaticUrlInfo(ZONE_NAME, ORIGINAL_URL, Stage.TEST);
        StaticUrlInfo urlInfo = this.initStaticUrlInfo(staticUrlInfo, ARBITRARY_BRANCH);
        String serviceUrl = urlInfo.getRecordSetName();
        assertThat(serviceUrl, matchesPattern("test\\.*\\." + ORIGINAL_URL));
    }

}
