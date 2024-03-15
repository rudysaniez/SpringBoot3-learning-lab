package com.springboot.learning.authorization.tests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

/**
 * Tests for {@link OAuth2AuthorizationServerApplication}.
 *
 * @author Josh Cummings
 */
@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false"})
@AutoConfigureMockMvc
class OAuth2AuthorizationServerApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void requestTokenUsingClientCredentialsGrantType() throws Exception {

        String base64Credentials = Base64.getEncoder().encodeToString("writer:secret-writer".getBytes());
        System.out.println(" > writer:secret-writer in base64=" + base64Credentials);
        this.mvc.perform(post("/oauth2/token")
                        .param("grant_type", "client_credentials")
                        .header("Authorization", "Basic " + base64Credentials))
                .andExpect(status().isOk());
    }

    @Test
    void requestOpenidConfiguration() throws Exception {

        this.mvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(status().isOk());
    }

    @Test
    void requestJwkSet() throws Exception {

        this.mvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk());
    }

    @Test
    void healthy() throws Exception {
        this.mvc.perform(get("/management/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

}
