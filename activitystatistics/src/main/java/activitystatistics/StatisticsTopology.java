package activitystatistics;

import activitystatistics.model.User;
import activitystatistics.model.Activity;
import activitystatistics.model.StatisticEvent;
import activitystatistics.model.join.Enriched;
import activitystatistics.model.join.StatisticWithActivity;
import activitystatistics.serialization.json.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.state.KeyValueStore;

class StatisticsTopology {

  public static Topology build() {
    StreamsBuilder builder = new StreamsBuilder();

    // Register the activity statistics streams
    // "Statistics" are streamed events without an own key therefore we use
    // KStream abstraction and select the key of the corresponding activity to later join with
    // (enrich our statistics event with infos about the user and the activity itself)
    KStream<String, StatisticEvent> statisticEvents =
        builder
            .stream("statistic-events", Consumed.with(Serdes.ByteArray(), JsonSerdes.StatisticEvent()))
            // now marked for re-partitioning
            .selectKey((k, v) -> v.getActivityId().toString());

    // Create the sharded activity table
    KTable<String, Activity> activities =
            builder.table("activities", Consumed.with(Serdes.String(), JsonSerdes.Activity()));

    // Create the sharded user table
    KTable<String, User> users =
        builder.table("users", Consumed.with(Serdes.String(), JsonSerdes.User()));

    // Join statistic-events with activities
    Joined<String, StatisticEvent, Activity> activityJoinParams =
        Joined.with(Serdes.String(), JsonSerdes.StatisticEvent(), JsonSerdes.Activity());
    ValueJoiner<StatisticEvent, Activity, StatisticWithActivity> statisticActivityJoiner =
        (statistic, activity) -> new StatisticWithActivity(statistic, activity);
    KStream<String, StatisticWithActivity> withActivity =
        statisticEvents.join(activities, statisticActivityJoiner, activityJoinParams)
                // select user id as the new key
                .selectKey((k, v) -> v.getStatisticEvent().getUserId().toString());

    // Join statistics-with-activities with users
    Joined<String, StatisticWithActivity, User> userJoinParams =
            Joined.with(Serdes.String(), JsonSerdes.StatisticWithActivity(), JsonSerdes.User());
    ValueJoiner<StatisticWithActivity, User, Enriched> userJoiner =
            (statisticWithActivity, user) -> new Enriched(statisticWithActivity, user);
    KStream<String, Enriched> withUsers = withActivity.join(users, userJoiner, userJoinParams);
    withUsers.print(Printed.<String, Enriched>toSysOut().withLabel("with-users"));

    // Group the enriched stream
    KGroupedStream<String, Enriched> grouped =
        withUsers.groupBy(
            // Here we could also group by users instead of activities
            (key, value) -> value.getActivityId().toString(),
            Grouped.with("grouped-enriched", Serdes.String(), JsonSerdes.Enriched()));

    // The initial value of our aggregation will be a new activity statistic instance
    Initializer<ActivityStatistic> activityStatisticInitializer = ActivityStatistic::new;

    // The logic for aggregating statistics is implemented in the ActivityStatistic.add method
    Aggregator<String, Enriched, ActivityStatistic> activityStatisticAdder =
        (key, value, aggregate) -> aggregate.add(value);

    // Perform the aggregation, and materialize the underlying state store for querying
    KTable<String, ActivityStatistic> activityStatistics =
        grouped.aggregate(
            activityStatisticInitializer,
            activityStatisticAdder,
            Materialized.<String, ActivityStatistic, KeyValueStore<Bytes, byte[]>>
                // give the state store an explicit name to make it available for interactive
                // queries
                as("activity-statistics-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(JsonSerdes.ActivityStatistic()));

    activityStatistics.toStream().to("activity-statistics");

    return builder.build();
  }
}
