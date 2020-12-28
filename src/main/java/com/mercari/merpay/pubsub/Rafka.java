package com.mercari.merpay.pubsub;

import static spark.Spark.port;
import static spark.Spark.threadPool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mercari.merpay.pubsub.properties.ApplicationProperties;
import com.mercari.merpay.pubsub.routing.Routing;
import java.io.IOException;
import lombok.Value;

/**
 * Application entrypoint. Properties are read from application.properties resource and injected to {@link Routing}.
 */
@Value
public class Rafka {

  ApplicationProperties properties;

  public Rafka() {
    properties = new ApplicationProperties();
  }

  public static void main(String[] args) throws IOException {
    new Rafka().listenAndServe();
  }

  public void listenAndServe() throws IOException {
    properties.load();
    port(Integer.parseInt(properties.getProperty("server.port")));
    threadPool(
        Integer.parseInt(properties.getProperty("server.threads.max")),
        Integer.parseInt(properties.getProperty("server.threads.min")),
        Integer.parseInt(properties.getProperty("server.timeout.idle.millis"))
    );
    Gson gson = new Gson();
    Routing.builder()
        .staticDir(properties.getProperty("server.resources.static.path"))
        .maxContentLength(Integer.parseInt(properties.getProperty("message.length.max.bytes")))
        .encoder(gson::toJson)
        .decoder((json) -> gson.fromJson(json, JsonObject.class))
        .dataDir(properties.getProperty("data.directory"))
        .build()
        .register();
  }
}
