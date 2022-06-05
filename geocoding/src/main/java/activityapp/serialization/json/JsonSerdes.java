package activityapp.serialization.json;

import activityapp.GeolocationHistory;
import activityapp.model.Geolocation;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {

    public static Serde<Geolocation> Geolocation() {
        JsonSerializer<Geolocation> serializer = new JsonSerializer<>();
        JsonDeserializer<Geolocation> deserializer = new JsonDeserializer<>(Geolocation.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<GeolocationHistory> GeolocationHistory() {
        JsonSerializer<GeolocationHistory> serializer = new JsonSerializer<>();
        JsonDeserializer<GeolocationHistory> deserializer = new JsonDeserializer<>(GeolocationHistory.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

}
