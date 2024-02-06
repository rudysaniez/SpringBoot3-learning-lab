package com.springboot.learning.sb3.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.Duration;

public class TestHelper {

    private static final Logger log = LoggerFactory.getLogger(TestHelper.class);

    /**
     * @param jack : Jack !
     * @param input : the input resource
     * @param type : the type
     * @return {@link T}
     * @param <T> : the parameterized type
     * @throws IOException
     */
    public static <T> T getAttributeCandidate(ObjectMapper jack, Resource input, Class<T> type) throws IOException {
        return jack.readValue(input.getInputStream(), type);
    }

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
}
