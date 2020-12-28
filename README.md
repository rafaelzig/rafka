# Background

### Use case graph:

<img src="./image/image1.png" width="700px">

### Use case diagram:

<img src="./image/image2.png" width="700px">

Publisher and Subscriber are other microservice servers inside the company.

Phase 1: Must release the server to production within
one week. 

Phase 2: Another team will continue to improve the server based on your codes implemented
and the phase2Plan.md you make in Phase 1.

# Requirements

## Phase 1 Requirements

1. Use Java for implementation.
1. The maximum size of one message is 128KB.
1. Provide Dockerfile and other necessary materials to the SRE team so that they can deploy the
   server appropriately.
1. Estimated system workload: 200 rps, 99% latency within one second.
1. Phase1 is more like an emergency rescue solution, it’s acceptable to lost messages when the
   server running into unexpected downtime.
1. Keep in mind that this repository is the only information source for other roles, including
   Publisher, Subscriber, SRE team and the team who will work on this project in Phase2.
1. Write your technical plan in phase2Plan.md file, so that the team who will take over this project
   could work on based on your code repository. Don’t need to obstinate to the architecture of
   well-known message queues, such as Apache Kafka and RabbitMQ, make the technical plan based on
   your own backend development experience and the team capacity for Phase 2 is good enough.

## Phase 2 Requirements

1. Estimated system workload: 20000 rps, 99% latency within one second. While sometimes business
   growth much faster than our prediction.
1. Messages should NOT be lost unless hardware-level incident.


