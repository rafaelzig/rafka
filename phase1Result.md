# Phase 1 Requirements

1. Use Java for implementation.
    1. DONE
1. The maximum size of one message is 128KB.
    1. Configurable via property `message.length.max.bytes`
       in **[application.properties](src/main/resources/application.properties)**
1. Provide Dockerfile and other necessary materials to the SRE team so that they can deploy the
   server appropriately.
    1. [Dockerfile](Dockerfile) for deployments
    1. [docker-compose.yaml](docker-compose.yaml) for local environment
1. Estimated system workload: 200 rps, 99% latency within one second.
    1. Publisher and subscriber load test run concurrently with 200 rps each:
        1. [Publisher Load Test Results](gatling/results/publisher/baseline/index.html)
        1. [Subscriber Load Test Results](gatling/results/subscriber/baseline/index.html)
    1. Please refer
       to [Publisher.scala](gatling/user-files/simulations/com/mercari/merpay/pubsub/Publisher.scala)
       and [Subscriber.scala](gatling/user-files/simulations/com/mercari/merpay/pubsub/Subscriber.scala)
       for more information about the load test plans.
    1. These can be repeated by running `docker-compose up` on the root directory of this project.
1. Phase1 is more like an emergency rescue solution, it’s acceptable to lost messages when the
   server running into unexpected downtime.
    1. Messages are **NOT** lost unless hardware-level incident.
1. Keep in mind that this repository is the only information source for other roles, including
   Publisher, Subscriber, SRE team and the team who will work on this project in Phase2.
    1. Example http calls are given with respective documentation:
        1. [ack.http](examples/message/ack.http)
        1. [get.http](examples/message/get.http)
        1. [publish.http](examples/message/publish.http)
        1. [register.http](examples/topic/register.http)
        1. [subscribe.http](examples/topic/subscribe.http)
1. Write your technical plan in phase2Plan.md file, so that the team who will take over this project
   could work on based on your code repository. Don’t need to obstinate to the architecture of
   well-known message queues, such as Apache Kafka and RabbitMQ, make the technical plan based on
   your own backend development experience and the team capacity for Phase 2 is good enough.
    1. [phase2Plan.md](phase2Plan.md)