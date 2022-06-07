package activitystatistics.serialization.json;

import activitystatistics.ActivityStatistic;
import activitystatistics.model.User;
import activitystatistics.model.Activity;
import activitystatistics.model.StatisticEvent;
import activitystatistics.model.join.Enriched;
import activitystatistics.model.join.StatisticWithActivity;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {

  public static Serde<ActivityStatistic> ActivityStatistic() {
    JsonSerializer<ActivityStatistic> serializer = new JsonSerializer<>();
    JsonDeserializer<ActivityStatistic> deserializer = new JsonDeserializer<>(ActivityStatistic.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<Enriched> Enriched() {
    JsonSerializer<Enriched> serializer = new JsonSerializer<>();
    JsonDeserializer<Enriched> deserializer = new JsonDeserializer<>(Enriched.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<StatisticWithActivity> StatisticWithActivity() {
    JsonSerializer<StatisticWithActivity> serializer = new JsonSerializer<>();
    JsonDeserializer<StatisticWithActivity> deserializer = new JsonDeserializer<>(StatisticWithActivity.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<StatisticEvent> StatisticEvent() {
    JsonSerializer<StatisticEvent> serializer = new JsonSerializer<>();
    JsonDeserializer<StatisticEvent> deserializer = new JsonDeserializer<>(StatisticEvent.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<User> User() {
    JsonSerializer<User> serializer = new JsonSerializer<>();
    JsonDeserializer<User> deserializer = new JsonDeserializer<>(User.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<Activity> Activity() {
    JsonSerializer<Activity> serializer = new JsonSerializer<>();
    JsonDeserializer<Activity> deserializer = new JsonDeserializer<>(Activity.class);
    return Serdes.serdeFrom(serializer, deserializer);
  }
}
