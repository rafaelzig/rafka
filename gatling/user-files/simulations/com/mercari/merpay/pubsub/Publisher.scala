package com.mercari.merpay.pubsub

import scala.concurrent.duration._

class Publisher extends Simulation {
  /** Configure endpoint, qps and duration */
  val protocol = System.getProperty("protocol", "http")
  val host = System.getProperty("target", "rafka")
  val rps = Integer.getInteger("rps", 200).doubleValue()
  val rampUpDurationMinutes = Integer.getInteger("rampUpDuration", 1)
  val holdDurationMinutes = Integer.getInteger("holdDuration", 1)

  /** ''before'' / ''after'' hooks - use for STDOUT text output for now */
  before {
    println("================================================================================")
    println("---- Starting Publisher Load Test ----------------------------------------------")
    println("================================================================================")
  }

  after {
    println("================================================================================")
    println("---- Finishing Publisher Load Test ---------------------------------------------")
    println("================================================================================")
  }

  val httpProtocol = http.baseUrl(protocol + "://" + host)
    .acceptHeader("application/json")
    .acceptCharsetHeader("utf-8")
  val register = scenario("register").exec(Register.register)
  val publish = scenario("publish").exec(Publish.publish)

  setUp(
    register.inject(
      atOnceUsers(1)
    ),
    publish.inject(
      nothingFor(5 second), // Wait for registrations (sequential scenarios are supported from version 3.4 released in September 2020)
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

object Register {
  private val name = "/topic/register"
  val register = exec(http(name)
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

object Publish {
  val publish = feed(jsonFile("publish.json").random)
    .exec(
      http("${apiName}")
        .post("${path}")
        .body(StringBody("""${body}"""))
        .header("Content-Type", "application/json; charset=utf-8")
        .check(status.is(201))
    )
}
