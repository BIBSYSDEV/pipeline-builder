package no.bibsys.aws.route53;

import no.bibsys.aws.cloudformation.Stage;

public final class NetworkInfo {


    private final transient String recordSetName;
    private final transient String domainName;
    private final transient String zoneName;


    private NetworkInfo(String zoneName, String recordSetName) {
        this.zoneName = zoneName;
        this.recordSetName = recordSetName;
        this.domainName = recordSetName.substring(0, recordSetName.length() - 1);
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

    public static NetworkInfo create(Stage stage, String zoneName, String recordSetName) {
        if (stage.equals(Stage.FINAL)) {
            return new NetworkInfo(zoneName, recordSetName);
        } else {
            return new NetworkInfo(zoneName, "test." + recordSetName);
        }

    }


}
