package com.springboot.learning.sb3.cucumber.glues;

import com.springboot.learning.sb3.controller.contract.v1.AttributeDictionary;
import com.springboot.learning.sb3.cucumber.glues.config.GlueConfiguration;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AttributeDictionarySearchGlue extends GlueConfiguration {

    AtomicReference<String> attributeDictionaryCodeContext = new AtomicReference<>();
    AtomicInteger httpStatusContext = new AtomicInteger();
    AtomicReference<List<AttributeDictionary>> attributesFoundContext = new AtomicReference<>();

    private static final Logger log = LoggerFactory.getLogger(AttributeDictionarySearchGlue.class);

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = getPort();
        log.info(" > The baseURI is {} and port is {}.", RestAssured.baseURI, RestAssured.port);
    }

    @Given("I search this attribute dictionary with code equals to {string}")
    public void I_search_this_attribute_dictionary_with_code_equals_code01(String code) {
        log.info(" > I_search_this_attribute_dictionary_with_code_equals_code01");
        attributeDictionaryCodeContext.set(code);
    }

    @When("I call a get in reactive api with the code searched")
    public void I_call_a_get_in_reactive_api_with_the_code_searched() {
        log.info(" > I call a get in reactive api with the code searched {}", attributeDictionaryCodeContext.get());

        final Response response = RestAssured.given()
            .auth().basic("user", "user")
                .get("/v1/attributes/:search?q=code=" + attributeDictionaryCodeContext.get());

        httpStatusContext.set(response.statusCode());
        attributesFoundContext.set(response.body().as(new TypeRef<>() {}));
    }

    @Then("I have a HTTP status equals {int} but the content is empty")
    public void iHaveAHTTPStatusEqualsButTheContentIsEmpty(int httpStatus) {
        Assertions.assertThat(httpStatusContext.get()).isEqualTo(httpStatus);
        Assertions.assertThat(attributesFoundContext.get()).isEmpty();
    }
}
