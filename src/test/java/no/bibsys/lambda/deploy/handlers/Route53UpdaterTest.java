package no.bibsys.lambda.deploy.handlers;

import org.junit.Test;

public class Route53UpdaterTest {


    @Test
    public void foo() {
        Route53Updater route53Updater = new Route53Updater() {
        };
        route53Updater.createRecordSet();
    }


}
