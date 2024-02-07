package com.springboot.learning.sb3.cucumber.glues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.controller.contract.v1.AttributeDictionary;
import com.springboot.learning.sb3.cucumber.glues.config.GlueConfiguration;
import com.springboot.learning.sb3.helper.TestHelper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AttributeDictionarySearchInDatabaseFilledGlue extends GlueConfiguration {

    AtomicReference<String> attributeCodeContext = new AtomicReference<>();
    AtomicInteger httpStatusContext = new AtomicInteger();
    AtomicReference<List<AttributeDictionary>> attributesFoundContext = new AtomicReference<>();

    @Autowired
    ObjectMapper jack;

    @Value("classpath:json/attributes.json")
    Resource attributes;

    private static final Logger log = LoggerFactory.getLogger(AttributeDictionarySearchInDatabaseFilledGlue.class);

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = getPort();
        log.info(" > The baseURI is {} and port is {}.", RestAssured.baseURI, RestAssured.port);
    }

    @Given("I prepare the search in database filled with code equals to {string}")
    public void iPrepareTheSearchInDatabaseFilledWithCodeEqualsTo200(String code) {

        log.info(" > I prepare the search in database filled with code equals to {}", code);
        attributeCodeContext.set(code);

        // Inject several attributes.
        List<AttributeDictionary> attributeDictionaryList = TestHelper.getManyAttributeCandidates(jack, attributes);
        final var json = TestHelper.getJsonByGoodOldJack(jack, attributeDictionaryList);

        final Response response = RestAssured.given()
                .auth().basic("user", "user")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("/v1/attributes/:bulk");
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
    }

    @When("I call a get in reactive api with the code searched in database filled")
    public void iCallAGetInReactiveApiWithTheCodeSearchedInDatabaseFilled() {

        log.info(" > I call a get in reactive api with the code searched in database filled {}",
                attributeCodeContext.get());

        TestHelper.waitInSecond(1);
        final Response response = RestAssured.given()
            .auth().basic("user", "user")
            .get("/v1/attributes/:search?q=code=" + attributeCodeContext.get());

        httpStatusContext.set(response.statusCode());
        attributesFoundContext.set(response.body().as(new TypeRef<>() {}));
    }

    @Then("I have a HTTP status equals {int} and the content is not empty")
    public void iHaveAHTTPStatusEquals200AndTheContentIsNotEmpty(int httpStatus) {
        Assertions.assertThat(httpStatusContext.get()).isEqualTo(httpStatus);
        Assertions.assertThat(attributesFoundContext.get()).isNotEmpty();
        Assertions.assertThat(attributesFoundContext.get().getFirst().code())
                .isEqualTo(attributeCodeContext.get().toUpperCase());
    }
}
