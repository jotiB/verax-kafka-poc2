# Sample Programs for Kafka 0.10 API

This project provides a simple but realistic example of a Kafka
producer and consumer. These programs are written in a style and a
scale that will allow you to adapt them to get something close to a
production style. There is a surprising dearth of examples for the new
Kafka API that arrived with 0.10.0, which is a pity since the new API
is so much better than the previous API.

This README takes you through the steps for downloading and installing
a single node version of Kafka. We don't focus on the requirements for
a production Kafka cluster because we want to focus on the code itself
and various aspects of starting and restarting.

## Pre-requisites
To start, you need to get Kafka up and running and create some topics.

### Step 1: Download Kafka
Download the 0.10.1.1 release and un-tar it.
```
$ tar -xzf kafka_2.11-0.10.1.1.tgz
$ cd kafka_2.11-0.10.1.1
```
### Step 2: Start the server
Start a ZooKeeper server. Kafka has a single node Zookeeper configuration built-in.
```
$ bin/zookeeper-server-start.sh config/zookeeper.properties &
[2013-04-22 15:01:37,495] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
...
```
Note that this will start Zookeeper in the background. To stop
Zookeeper, you will need to bring it back to the foreground and use
control-C or you will need to find the process and kill it.

Now start Kafka itself:
```
$ bin/kafka-server-start.sh config/server.properties &
[2013-04-22 15:01:47,028] INFO Verifying properties (kafka.utils.VerifiableProperties)
[2013-04-22 15:01:47,051] INFO Property socket.send.buffer.bytes is overridden to 1048576 (kafka.utils.VerifiableProperties)
...
```
As with Zookeeper, this runs the Kafka broker in the background. To
stop Kafka, you will need to bring it back to the foreground or find
the process and kill it explicitly using `kill`.

### Step 3: Create the topic for the example program

```
$ bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic stock-quotes

```
These can be listed
```
$ bin/kafka-topics.sh --list --zookeeper localhost:2181

```
Note that you will see log messages from the Kafka process when you
run Kafka commands. You can switch to a different window if these are
distracting.

The broker can be configured to auto-create new topics as they are mentioned, but that is often considered a bit 
dangerous because mis-spelling a topic name doesn't cause a failure.

## Now for the real fun
At this point, you should have a working Kafka broker running on your
machine. The next steps are to compile the example programs and play
around with the way that they work.

### Step 4: Compile and package up the example programs
Go back to the directory where you have the example programs and
compile and build the example programs.
```
$ cd ..
$ mvn package
...
```

For convenience, the example programs project is set up so that the
maven `package` target produces a single executable,
`target/kafka-example`, that includes all of the example programs and
dependencies.

### Step 5: Run the example producer

The producer pulls stock quotes from the various stock exchange in streaming mode.

To run it, copy the generated artifacts into your home directory on the machine where Kafka Broker is installed and run
the following command.

kafka-stock-quotes producer interval period <file path> limit

where

interval is in seconds
period is in days
file path is the path name of the file containing a list of symbols within exchanges
limit is the maximum number of quotes 
 

Note: You need to have access to internet before calling this API.


The producer will send a large number of messages to `stock-quotes`. Since there isn't
any consumer running yet, nobody will receive the messages. 

```

```
### Step 6: Start the example consumer
Running the consumer will not actually cause any messages to be
processed. The reason is that the first time that the consumer is run,
this will be the first time that the Kafka broker has ever seen the
consumer group that the consumer is using. That means that the
consumer group will be created and the default behavior is to position
newly created consumer groups at the end of all existing data.
```
$ target/kafka-stock-quotes consumer
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```
After running the consumer once, however, if we run the producer again
and then run the consumer *again*, we will see the consumer pick up
and start processing messages shortly after it starts.


```
$ target/kafka-stock-quotes consumer
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.

```
Note that there is a significant latency for
the messsage batches. This is because the consumer wasn't running when
the message were sent to Kafka and thus it is only getting them much
later, long after they were sent.

The consumer should, however, gnaw its way through the backlog pretty
quickly, however and the per batch latency should be shorter by the
end of the run than at the beginning. If the producer is still running
by the time the consumer catches up, the latencies will probably drop
into the single digit millisecond range.

### Step 7: Send more messages
In a separate window, run the producer again without stopping the
consumer. Note how the messages are displayed almost instantly by the
consumer and the latencies measured by the consumer are now quite
small, especially compared to the first time the consumer was run.

This isn't a real production-scale benchmark, but it does show that
two processes can send and receive messages at a pretty high rate and
with pretty low latency.

## Fun Step: Mess with the consumer

While the producer is producing messages (you may want to put it in a
loop so it keeps going) and the consumer is eating them, try killing
and restarting the consumer. The new consumer will wait for about 10
seconds before Kafka assigns it to the partitions that the killed
consumer was handling. Once the consumer gets cooking, however, the
latency on the records it is processing should drop quickly to the
steady state rate of a few milliseconds. You can have similar effect
by using control-Z to pause the consumer for a few seconds but if you
do that, the consumer should restart processing immediately as soon as
you let it continue. The way that this works is if you pause for a
short time, the consumer still has the topic partition assigned to it
by the Kafka broker, so it can start right back up. On the other hand,
if you pause for more than about 10 seconds, the broker will decide
the consumer has died and that there is nobody available to handle the
partitions in the topic for that consumer group. As soon as the
consumer program comes back, however, it will reclaim ownership and
continue reading data.

## Remaining Mysteries
If you change the consumer properties, particular the buffer sizes
near the end of properties file, you may notice that the
consumer can easily get into a state where it has about 5 seconds of
timeouts during which no data comes from Kafka and then a full
bufferful arrives. Once in this mode, the consumer tends to not
recover to normal processing. It isn't clear what is going on, but
setting the buffer sizes large enough can avoid the problem.

## Cleaning Up
When you are done playing, stop Kafka and Zookeeper and delete the
data directories they were using from /tmp

```
$ fg
bin/kafka-server-start.sh config/server.properties
^C
$ fg
bin/zookeeper-server-start.sh config/zookeeper.properties
^C
$ rm -rf /tmp/zookeeper/version-2/log.1  ; rm -rf /tmp/kafka-logs/
$
```

## Credits
Note that this example was derived in part from the documentation provided by the Apache Kafka project. We have 
added short, realistic sample programs that illustrate how real programs are written using Kafka.  
