package no.bibsys.aws.route53;

import no.bibsys.aws.cloudformation.Stage;


/**
 * Information for creating a mapping of a dynamic API Gateway url to a static url.
 *<p>Terms:
 * <ul>
 * <li>Zone name: The name of the Route53 Hosted Zone</li>
 * <li>Record set name: The name of a CNAME RecordSet in the Route53 Hosted Zone</li>
 * <li>Domain name: The Name of an API Gateway Custom-Domain-Entry</li>
 * </ul>
 * The Record set name and the Domain entry must be identical with the exception that the Record set name ends with a
 * fullstop (.) while the Domain entry does not.
 * </p>
 */
public final class StaticUrlInfo {


    private final transient String recordSetName;
    private final transient String domainName;
    private final transient String zoneName;


    private StaticUrlInfo(String zoneName, String recordSetName) {
        this.zoneName = zoneName;
        this.recordSetName = recordSetName;
        this.domainName = recordSetName.substring(0, recordSetName.length() - 1);
    }

    public static StaticUrlInfo create(Stage stage, String zoneName, String recordSetName) {
        if (stage.equals(Stage.FINAL)) {
            return new StaticUrlInfo(zoneName, recordSetName);
        } else {
            return new StaticUrlInfo(zoneName, "test." + recordSetName);
        }

    }

    public String getRecordSetName() {
        return recordSetName;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getZoneName() {
        return zoneName;
    }


}
