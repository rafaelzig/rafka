# Phase 2 Requirements

1. Estimated system workload: 20000 rps, 99% latency within one second.
1. Messages should NOT be lost unless hardware-level incident.

## Requirement 1: Throughput

### Cache layer

We can improve the current throughput performance by committing changes to the state of the server
to a cache layer.

For each client request, we can query/update the cache layer accordingly. We can then respond to
clients and asynchronously persist the state changes to disk.

It is worth to note that, by implementing this feature, we sacrifice the ability to sustain message
loss in the events of hardware failures (disk fails during asynchronous call to persist a message to
disk after the server issues a response to a client).

Therefore, this behaviour should be enabled or disabled via configuration properties so that we can
customize the fault tolerance options of the server.

### Vertical scaling

We can improve the current throughput performance by running the server on a machine with higher
specifications, especially faster I/O.

This might not be the best option since we have a single point of failure.

### Horizontal Scaling

We can deploy multiple instances of the server into a cluster, preferably in different machines for
redundancy purposes.

This particular approach can be tackled similarly to other well-established solutions such as Apache
Kafka.

Rafka maintains the following data elements:

- Subscriber offset (per subscriber)
- Topic messages (per topic)

Therefore, we need to devise a partitioning logic for each of these data elements.

Each instance must be able to serve an individual request as before, but only requiring the view of
its own partitioned data.

The cluster must also be able to load-balance requests to all instances.

## Requirement 2

The current application already achieves this requirement.

Server state is persisted to disk upon each client interaction:

1. topic registration
1. topic subscription
1. message publication
1. message acknowledgment

## Further improvements

1. Publishers and subscribers are identified via their IP address, or the IP address of the last
   proxy that sent the request. This is not foolproof and can be spoofed.
    1. We can request clients to be authenticated on a per-request basis via HTTP Authorization
       request headers. We can then use the authentication principal as the identity of clients
       instead of their IP.
1. Only the mime-type 'application/json' and charset 'utf-8' is supported.
    1. We can provide support for multiple mime-types and charsets.
1. Traffic is not compressed, resulting in larger network bandwidth utilization.
    1. We can provide support for content encoding to compress the traffic.
1. If we scale the cluster horizontally (please see above), messages can be lost in the event of a
   hardware-level failures of single nodes in the cluster.
    1. To improve the fault tolerance guarantees of the system we have to also tackle the problem of
       maintaining the overall health of the cluster and ensuring data is properly replicated across
       the cluster nodes. This is a distributed system problem in itself, requiring a leader
       election mechanism, replication management, etc. Once more, we could look at well-established
       solutions such as Apache ZooKeeper to solve these problems.