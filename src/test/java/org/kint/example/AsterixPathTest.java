package org.kint.example;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Demonstrate using asterix paths in web server")
@ExtendWith(VertxExtension.class)
class AsterixPathTest {
  @Test
  @DisplayName("Should echo request body in response")
  void shouldEchoRequestBodyInResponse(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = testContext.checkpoint();
    Checkpoint requestCheckpoint = testContext.checkpoint(1);

    vertx.deployVerticle(new AsterixPathVerticle(), testContext.succeeding(id -> {
      deploymentCheckpoint.flag();

      JsonObject requestBody = new JsonObject()
        .put("greeting", "hello");

      webClient.post(11981, "localhost", "/my-path")
        .as(BodyCodec.string())
        .sendJsonObject(requestBody, testContext.succeeding(resp -> {
          testContext.verify(() -> {
            assertThat(resp.statusCode()).isEqualTo(201);

            JsonObject responseBody = new JsonObject(resp.body());

            assertThat(responseBody.getString("greeting")).isEqualTo("hello");

            requestCheckpoint.flag();
          });
        }));
    }));
  }

  @Test
  @DisplayName("Should be able to get resource with required header")
  void shouldBeAbleToGetResourceWithRequiredHeader(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = testContext.checkpoint();
    Checkpoint requestCheckpoint = testContext.checkpoint(1);

    vertx.deployVerticle(new AsterixPathVerticle(), testContext.succeeding(id -> {
      deploymentCheckpoint.flag();

      webClient.get(11981, "localhost", "/my-path")
        .as(BodyCodec.string())
        .putHeader("example-required-header", "bar")
        .send(testContext.succeeding(response -> {
          testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(200);

            JsonObject responseBody = new JsonObject(response.body());
            assertThat(responseBody.getString("example-property")).isEqualTo("foo");

            requestCheckpoint.flag();
          });
        }));
    }));
  }

  @Test
  @DisplayName("Should not be able to get resource without required header")
  void shouldNotBeAbleToGetResourceWithoutRequiredHeader(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = testContext.checkpoint();
    Checkpoint requestCheckpoint = testContext.checkpoint(1);

    vertx.deployVerticle(new AsterixPathVerticle(), testContext.succeeding(id -> {
      deploymentCheckpoint.flag();

      webClient.get(11981, "localhost", "/my-path")
        .as(BodyCodec.string())
        .send(testContext.succeeding(response -> {
          testContext.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.body()).isEqualTo("`example-required-header` must be provided");

            requestCheckpoint.flag();
          });
        }));
    }));
  }
}
