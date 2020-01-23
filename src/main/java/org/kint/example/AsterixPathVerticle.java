package org.kint.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsterixPathVerticle extends AbstractVerticle {
  private final Logger logger = LoggerFactory.getLogger(AsterixPathVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    Router router = Router.router(vertx);

    router.post("/my-path/*").handler(BodyHandler.create());
    router.post("/my-path").handler(this::handlePost);

    router.get("/my-path/*").handler(this::requireHeader);
    router.get("/my-path").handler(this::handleGet);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(11981, ar -> {
        if (ar.succeeded()) {
          startPromise.complete();
        } else {
          startPromise.fail(ar.cause());
        }
      });
  }

  private void requireHeader(RoutingContext routingContext) {
    final String REQUIRED_HEADER_NAME = "example-required-header";

    if (routingContext.request().headers().contains(REQUIRED_HEADER_NAME)) {
      routingContext.next();
    }
    else {
      HttpServerResponse response = routingContext.response();

      response.setStatusCode(400);
      response.putHeader("content-type", "text/plain");
      response.end(String.format("`%s` must be provided", REQUIRED_HEADER_NAME));
    }
  }

  private void handlePost(RoutingContext context) {
    HttpServerResponse response = context.response();

    if (StringUtils.isBlank(context.getBodyAsString())) {
      response.setStatusCode(400);
      response.putHeader("content-type", "text/plain");
      response.end("Body must be provided");
    } else {
      JsonObject body = context.getBodyAsJson();

      response.setStatusCode(201);
      response.putHeader("content-type", "application/json; charset=utf-8");

      response.end(body.encodePrettily());
    }

    logger.info("Handled a request on path {} from {}", context.request().path(),
      context.request().remoteAddress().host());
  }

  private void handleGet(RoutingContext context) {
    HttpServerResponse response = context.response();

    response.setStatusCode(200);
    response.putHeader("content-type", "application/json; charset=utf-8");

    JsonObject responseBody = new JsonObject().put("example-property", "foo");

    response.end(responseBody.encodePrettily());

    logger.info("Handled a request on path {} from {}", context.request().path(),
      context.request().remoteAddress().host());
  }
}
