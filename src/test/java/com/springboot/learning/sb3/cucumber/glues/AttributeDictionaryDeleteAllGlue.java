package com.springboot.learning.sb3.cucumber.glues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.learning.sb3.controller.contract.v1.AttributeDictionary;
import com.springboot.learning.sb3.controller.contract.v1.BulkResult;
import com.springboot.learning.sb3.controller.contract.v1.Page;
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

public class AttributeDictionaryDeleteAllGlue extends GlueConfiguration {

    AtomicInteger httpStatusContext = new AtomicInteger();
    AtomicReference<List<BulkResult>> bulkResultContext = new AtomicReference<>();

    @Autowired
    ObjectMapper jack;

    @Value("classpath:json/attributes.json")
    Resource attributes;

    private static final Logger log = LoggerFactory.getLogger(AttributeDictionaryDeleteAllGlue.class);

    @Before
    public void setUp() {
        Assertions.assertThat(jack).isNotNull();
        Assertions.assertThat(attributes).isNotNull();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = getPort();
        log.info(" > The baseURI is {} and port is {}.", RestAssured.baseURI, RestAssured.port);
    }

    @Given("A database filled")
    public void aDatabaseFilled() {

        // Inject several attributes.
        List<AttributeDictionary> attributeDictionaryList = TestHelper.getManyAttributeCandidates(jack, attributes);
        final var json = TestHelper.getJsonByGoodOldJack(jack, attributeDictionaryList);

        final Response bulkResponse = RestAssured.given()
                .auth().basic("user", "user")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json)
                .post("/v1/attributes/:bulk");
        Assertions.assertThat(bulkResponse.statusCode()).isEqualTo(200);
        TestHelper.waitInSecond(1);
        log.info(" > A database filled");

        // Get a page of attributes
        final Response pageResponse = RestAssured.given()
                .auth().basic("user", "user")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("/v1/attributes?page=0&size=10");
        Assertions.assertThat(pageResponse.statusCode()).isEqualTo(200);
        Page<AttributeDictionary> page = pageResponse.getBody().as(new TypeRef<>() {});
        Assertions.assertThat(page.pageMetadata().totalElements()).isGreaterThan(1);
    }

    @When("I call a delete all in reactive api in database filled")
    public void iCallADeleteAllInReactiveApiInDatabaseFilled() {

        log.error("> I call a delete all in reactive api in database filled");

        Response response = RestAssured.given()
                .auth().basic("user", "user")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .delete("/v1/attributes/:empty");
        Assertions.assertThat(response.statusCode()).isEqualTo(200);

        httpStatusContext.set(response.statusCode());
        bulkResultContext.set(response.getBody().as(new TypeRef<>() {}));
    }

    @Then("I have a HTTP status equals {int} and the bulk result status is equal to {int}")
    public void iHaveAHTTPStatusEqualsAndTheBulkResultStatusIsEqualTo(int httpStatus, int bulkStatus) {
        Assertions.assertThat(httpStatusContext.get()).isEqualTo(httpStatus);
        bulkResultContext.get().forEach(bulkResult -> Assertions.assertThat(bulkResult.status()).isEqualTo(bulkStatus));
    }
}
