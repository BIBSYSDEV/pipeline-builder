package no.bibsys.aws.tools;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.regex.Matcher;
import org.junit.Test;

public class StringUtilsTest extends AmazonRestrictions {


    @Test
    public void normalizedStringsShoudComplyWithAmazonConstraints() {
        String branchName = "AUTREG-49_Delete_tables_from_DynamoDB_after_testing";
        String normalized = stringUtils.normalizeString(branchName);
        Matcher matcher = amazonPattern.matcher(normalized);
        assertThat(matcher.matches(), is(equalTo(true)));

    }

}
