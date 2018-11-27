package no.bibsys.aws.tools;

import java.util.regex.Pattern;


public class AmazonNamingRestrictions {

    protected static final StringUtils stringUtils = new StringUtils();
    protected static final String amazonRegEx = "[a-zA-Z][-a-zA-Z0-9]*";
    protected static final Pattern amazonPattern = Pattern.compile(amazonRegEx);


}
