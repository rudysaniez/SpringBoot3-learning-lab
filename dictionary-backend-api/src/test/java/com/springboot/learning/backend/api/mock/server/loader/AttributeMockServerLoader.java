package com.springboot.learning.backend.api.mock.server.loader;

import com.springboot.learning.backend.api.CommonBaseFunction;
import jakarta.validation.constraints.NotNull;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

public class AttributeMockServerLoader extends CommonBaseFunction {

    public static final String ATTRIBUTE_PATH_BY_ID = "/attributes/isHomeDeliverable";
    public static final String ATTRIBUTE_PATH_AS_PAGE = "/attributes";
    public static final Map<String, HttpRequest> REQUEST_MAP = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(AttributeMockServerLoader.class);

    /**
     * @param attributeMockServer : the mock server
     */
    public static void load(@NotNull ClientAndServer attributeMockServer) throws Exception {

        var request = HttpRequest.request()
                .withMethod("GET")
                .withPath(ATTRIBUTE_PATH_BY_ID)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        REQUEST_MAP.put(ATTRIBUTE_PATH_BY_ID, request);

        attributeMockServer.when(request)
            .respond(HttpResponse.response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withBody(getBodyByFileName("json/attribute01.json"))
                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
            );

        log.info(" > The {} path is mocked (mock-server).", ATTRIBUTE_PATH_BY_ID);
    }

    /**
     * @param attributeMockServer : the mock server
     */
    public static void loadPage(@NotNull ClientAndServer attributeMockServer) throws Exception {

        var request = HttpRequest.request()
                .withMethod("GET")
                .withPath(ATTRIBUTE_PATH_AS_PAGE)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        REQUEST_MAP.put(ATTRIBUTE_PATH_AS_PAGE, request);

        attributeMockServer.when(request)
            .respond(HttpResponse.response()
                .withStatusCode(HttpStatusCode.OK_200.code())
                .withBody(getBodyByFileName("json/attributePage01.json"))
                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
            );

        log.info(" > The {} path is mocked (mock-server).", ATTRIBUTE_PATH_AS_PAGE);
    }

    /**
     * @param abilityMockServer : the ability mock server
     */
    public static void verifyRequest(@NotNull ClientAndServer abilityMockServer,
                                     @NotNull String path,
                                     int count) {

        abilityMockServer.verify(REQUEST_MAP.get(path),
                VerificationTimes.exactly(count));
    }
}
