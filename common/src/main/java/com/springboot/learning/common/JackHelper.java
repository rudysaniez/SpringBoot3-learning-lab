package com.springboot.learning.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.dictionary.domain.AttributeDictionaryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public class JackHelper {

    private static final Logger log = LoggerFactory.getLogger(JackHelper.class);

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
     * @param jack : Jack !
     * @param input : the input resource
     * @return {@link List<AttributeDictionaryEntity>}
     */
    public static List<AttributeDictionaryEntity> getManyAttributeCandidates(ObjectMapper jack, Resource input) {

        try {
            return jack.readValue(input.getContentAsByteArray(), new TypeReference<>() {});
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return List.of();
    }

    /**
     * @param jack : Jack !
     * @param value : the object to be transformed into JSON
     * @return {@link String}
     */
    public static String getJsonByGoodOldJack(ObjectMapper jack, Object value) {

        try {
            return jack.writeValueAsString(value);
        }
        catch(JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }

        throw new IllegalArgumentException();
    }
}
