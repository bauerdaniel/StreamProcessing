package activitystatistics;

import java.util.Properties;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;

public class App {
  public static void main(String[] args) {

    // We allow the following system properties to be overridden
    String host = System.getProperty("host");
    String portString = System.getProperty("port");
    String stateDir = System.getProperty("stateDir");

    // Set default values if not specified
    // -Dhost=localhost -Dport=7000 -DstateDir=/tmp/kafka-streams
    if (host == null) host = "localhost";
    if (portString == null) portString = "7000";
    if (stateDir == null) stateDir = "/tmp/kafka-streams";

    int port =  Integer.parseInt(portString);
    String endpoint = String.format("%s:%s", host, port);

    // Set the required properties for running Kafka Streams
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
    props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);

    // Build the topology
    Topology topology = StatisticsTopology.build();
    System.out.println("Starting Activity Statistics Application");
    KafkaStreams streams = new KafkaStreams(topology, props);

    // Close Kafka Streams when the JVM shuts down (e.g. SIGTERM)
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    // Clean up local state since many of the tutorials write to the same location.
    // You should run this sparingly in production since it will force the state
    // store to be rebuilt on start up
    streams.cleanUp();

    // Start streaming
    streams.start();

    // Start the REST service
    HostInfo hostInfo = new HostInfo(host, port);
    ActivityStatisticsService service = new ActivityStatisticsService(hostInfo, streams);
    service.start();
  }
}
