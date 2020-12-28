package com.mercari.merpay.pubsub

import scala.concurrent.duration._

class Subscriber extends Simulation {
  /** Configure endpoint, qps and duration */
  val protocol = System.getProperty("protocol", "http")
  val host = System.getProperty("target", "rafka")
  val rps = Integer.getInteger("rps", 200).doubleValue()
  val rampUpDurationMinutes = Integer.getInteger("rampUpDuration", 1)
  val holdDurationMinutes = Integer.getInteger("holdDuration", 1)

  /** ''before'' / ''after'' hooks - use for STDOUT text output for now */
  before {
    println("================================================================================")
    println("---- Starting Subscriber Load Test ----------------------------------------------")
    println("================================================================================")
  }

  after {
    println("================================================================================")
    println("---- Finishing Subscriber Load Test ---------------------------------------------")
    println("================================================================================")
  }

  val httpProtocol = http.baseUrl(protocol + "://" + host)
    .acceptHeader("application/json")
    .acceptCharsetHeader("utf-8")
  val subscribe = scenario("subscribe").exec(Subscribe.subscribe)
  val getAndAck = scenario("getAndAck").exec(GetAndAck.getAndAck)

  setUp(
    subscribe.inject(
      nothingFor(5 second), // Wait for registrations from Publisher
      atOnceUsers(1)
    ),
    getAndAck.inject(
      nothingFor(10 second), // Wait for subscriptions (sequential scenarios are supported from version 3.4 released in September 2020)
      rampUsersPerSec(1) to rps during (rampUpDurationMinutes minutes),
      constantUsersPerSec(rps) during (holdDurationMinutes minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lte(3000), // Asserts maximum response time < 3s
      global.responseTime.mean.lte(1000), // Asserts mean response time < 1s
      global.successfulRequests.percent.is(100) // Asserts all requests are successful
    )
}

object Subscribe {
  private val name = "/topic/subscribe"
  val subscribe = exec(http(name)
    .post(name + "/foo")
    .check(status.is(201))
    )
    .exec(http(name)
      .post(name + "/bar")
      .check(status.is(201))
    )
    .exec(http(name)
      .post(name + "/hello")
      .check(status.is(201))
    )
    .exec(http(name)
      .post(name + "/world")
      .check(status.is(201))
    )
    .exec(http(name)
      .post(name + "/sayonara")
      .check(status.is(201))
    )
    .exec(http(name)
      .post(name + "/sekai")
      .check(status.is(201))
    )
}

object GetAndAck {
  val getAndAck = feed(jsonFile("getAndAck.json").circular)
    .doSwitch("${method}")(
    "GET" -> exec(
      http("${apiName}")
        .get("${path}")
        .check(status.in(200, 204))
    ),
    "POST" -> exec(
      http("${apiName}")
        .post("${path}")
        .check(status.in(201, 204))
    )
  )
}
