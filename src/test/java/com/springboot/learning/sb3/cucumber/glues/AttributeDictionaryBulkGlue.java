package com.springboot.learning.sb3.cucumber.glues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.controller.contract.AttributeDictionary;
import com.springboot.learning.sb3.controller.contract.BulkResult;
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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AttributeDictionaryBulkGlue extends GlueConfiguration {

    AtomicInteger httpStatusContext = new AtomicInteger();
    AtomicReference<List<AttributeDictionary>> attributesPreparedContext = new AtomicReference<>();
    AtomicReference<List<BulkResult>> bulkResultContext = new AtomicReference<>();

    @Autowired
    ObjectMapper jack;

    @Value("classpath:json/attributes.json")
    Resource attributes;

    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryBulkGlue.class);

    @Before
    public void setUp() {
        Assertions.assertThat(jack).isNotNull();
        Assertions.assertThat(attributes).isNotNull();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = getPort();
        log.info(" > The baseURI is {} and port is {}.", RestAssured.baseURI, RestAssured.port);
    }

    @Given("I prepare several attributes dictionary will be launched")
    public void iPrepareSeveralAttributesDictionaryWillBeLaunched() {
        log.info(" > I prepare several attributes dictionary will be launched");
        attributesPreparedContext.set(TestHelper.getManyAttributeCandidates(jack, attributes));
    }

    @When("I call a post bulk in reactive api with several attributes dictionary")
    public void iCallAPostBulkInReactiveApiWithSeveralAttributesDictionary() {
        log.info(" > I call a post bulk in reactive api with several attributes dictionary");

        final var json = TestHelper.getJsonByGoodOldJack(jack, attributesPreparedContext.get());

        final Response response = RestAssured.given()
                .auth().basic("user", "user")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("/v1/attributes/:bulk");

        httpStatusContext.set(response.statusCode());
        bulkResultContext.set(response.body().as(new TypeRef<>() {}));
    }

    @Then("I have a HTTP status equals {int} and the content is a list of bulk result")
    public void iHaveAHTTPStatusEqualsAndTheContentIsAListOfBulkResult(int httpStatus) {
        Assertions.assertThat(httpStatusContext.get()).isEqualTo(httpStatus);
        Assertions.assertThat(bulkResultContext.get()).isNotEmpty();

        StepVerifier.create(Flux.fromIterable(bulkResultContext.get()))
                .expectNextMatches(bulkResult -> bulkResult.status() == 201)
                .expectNextMatches(bulkResult -> bulkResult.status() == 201)
                .expectNextMatches(bulkResult -> bulkResult.status() == 201)
                .expectNextMatches(bulkResult -> bulkResult.status() == 201)
                .expectNextMatches(bulkResult -> bulkResult.status() == 201)
                .verifyComplete();
    }
}
