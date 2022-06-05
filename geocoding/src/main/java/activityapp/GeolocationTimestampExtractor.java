package activityapp;

import activityapp.model.TimeEvent;

import java.time.Instant;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

/**
 * This class allows us to use event-time semantics for purchase streams
 */
public class GeolocationTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        TimeEvent measurement = (TimeEvent) record.value();
        if (measurement != null && measurement.getTimestamp() != null) {
            String timestamp = measurement.getTimestamp();
            // System.out.println("Extracting timestamp: " + timestamp);
            return Instant.parse(timestamp).toEpochMilli();
        }
        // Fallback to stream time
        return partitionTime;
    }
}
