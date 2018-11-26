package no.bibsys;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaLoggingTest {

    @Test
    public void testLambdaLogging() throws Exception {
        Logger logger = LoggerFactory.getLogger(LambdaLoggingTest.class);
        
        logger.trace("TRACE...");
        logger.debug("DEBUG");
        logger.info("INFO!");
        logger.warn("WARN!!");
        logger.error("ERROR!!!");
    }
    
}
