package no.bibsys.aws.tools;

import java.util.regex.Pattern;


public class AmazonNamingRestrictions {

    protected final static StringUtils stringUtils = new StringUtils();
    protected final static String amazonRegEx = "[a-zA-Z][-a-zA-Z0-9]*";
    protected final static  Pattern amazonPattern = Pattern.compile(amazonRegEx);


}
