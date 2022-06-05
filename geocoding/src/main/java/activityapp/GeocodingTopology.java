package activityapp;

import activityapp.model.Geolocation;
import activityapp.serialization.json.JsonSerdes;

import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.Suppressed.BufferConfig;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.lucene.util.SloppyMath.haversinMeters;

class GeocodingTopology {
    private static final Logger log = LoggerFactory.getLogger(GeocodingTopology.class);

    public static Topology build() {

        StreamsBuilder builder = new StreamsBuilder();

        Consumed<String, Geolocation> geolocationConsumerOptions =
                Consumed.with(Serdes.String(), JsonSerdes.Geolocation())
                        .withTimestampExtractor(new GeolocationTimestampExtractor());

        // Consume geolocation events
        KStream<String, Geolocation> geolocationEvents =
                builder.stream("geolocation-events", geolocationConsumerOptions);

        // Create a geolocation time window
        // Tumbling Time Window
        TimeWindows timeWindow = TimeWindows.of(Duration.ofMinutes(5)).grace(Duration.ofMinutes(1));
        // Hopping Time Window
        // TimeWindows timeWindow = TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofSeconds(30));
        // SlidingWindows timeWindow = SlidingWindows.withTimeDifferenceAndGrace(Duration.ofSeconds(30), Duration.ofSeconds(0));
        // SessionWindows timeWindow = SessionWindows.with(Duration.ofSeconds(5));

        // Aggregate geolocation histories
        Initializer<GeolocationHistory> geolocationHistoryInitializer = GeolocationHistory::new;
        Aggregator<String, Geolocation, GeolocationHistory> geolocationAdder =
                (key, value, aggregate) -> aggregate.add(value);

        KTable<Windowed<String>, GeolocationHistory> geolocationHistories = geolocationEvents
                .groupByKey()
                .windowedBy(timeWindow)
                .aggregate(
                        geolocationHistoryInitializer,
                        geolocationAdder,
                        Materialized.<String, GeolocationHistory, WindowStore<Bytes, byte[]>>
                                        as("geolocation-history")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(JsonSerdes.GeolocationHistory()))
                .suppress(Suppressed.untilWindowCloses(BufferConfig.unbounded().shutDownWhenFull()));

        // Calculate distances for each geolocation history
        KStream<String, Double> distances = geolocationHistories
                .toStream()
                .mapValues(v -> haversinMeters(
                                v.first().getLatitude(),
                                v.first().getLongitude(),
                                v.last().getLatitude(),
                                v.last().getLongitude()))
                .filter((key, value) -> value >= 1000)
                .map((windowedKey, value) -> KeyValue.pair(windowedKey.key(), value));

        distances.to("geocoding", Produced.with(Serdes.String(), Serdes.Double()));

        distances.print(Printed.<String, Double>toSysOut().withLabel("geocoding"));
        geolocationEvents.print(Printed.<String, Geolocation>toSysOut().withLabel("geolocation"));

        return builder.build();
    }
}
