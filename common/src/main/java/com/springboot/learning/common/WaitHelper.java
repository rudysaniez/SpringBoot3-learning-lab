package com.springboot.learning.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class WaitHelper {

    private static final Logger log = LoggerFactory.getLogger(WaitHelper.class);

    /**
     * @param during : the duration
     */
    public static void waitInSecond(int during) {
        try {
           Thread.sleep(Duration.ofSeconds(during));
        }
        catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param during : the duration
     */
    public static void waitInMillis(int during) {
        try {
            Thread.sleep(Duration.ofMillis(during));
        }
        catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
