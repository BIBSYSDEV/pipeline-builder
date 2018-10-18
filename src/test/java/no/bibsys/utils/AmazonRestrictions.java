package no.bibsys.utils;

import java.util.regex.Pattern;

public class AmazonRestrictions {

    protected StringUtils stringUtils=new StringUtils();
    protected String amazonRegEx ="[a-zA-Z][-a-zA-Z0-9]*";
    protected Pattern amazonPattern = Pattern.compile(amazonRegEx);

}
