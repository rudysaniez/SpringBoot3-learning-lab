package com.springboot.learning.backend.api;

import com.springboot.learning.backend.api.mock.server.loader.AttributeMockServerLoader;
import com.springboot.learning.backend.api.mock.server.loader.DictionaryHealthMockServerLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockserver.integration.ClientAndServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MockServerConfigurationTest {

    protected static ClientAndServer dictionaryMockServer;

    private static final Logger log = LoggerFactory.getLogger(MockServerConfigurationTest.class);

    @BeforeAll
    public static void beforeClass() throws Exception {

        /*
        Dictionary attribute mock server
         */
        dictionaryMockServer = ClientAndServer.startClientAndServer(1080);

        if(dictionaryMockServer.isRunning()) {
            log.info(" > The attributeDictionaryMockServer is started on the port {}.", dictionaryMockServer.getPort());
            AttributeMockServerLoader.load(dictionaryMockServer);
            AttributeMockServerLoader.loadPage(dictionaryMockServer);
            DictionaryHealthMockServerLoader.load(dictionaryMockServer);
        }
    }

    @AfterAll
    protected static void stopMockServer() {
        if (dictionaryMockServer.isRunning()) {
            dictionaryMockServer.close();
            dictionaryMockServer.stop();
        }
    }

    public static ClientAndServer getAttributeDictionaryMockServer() {
        return dictionaryMockServer;
    }
}
