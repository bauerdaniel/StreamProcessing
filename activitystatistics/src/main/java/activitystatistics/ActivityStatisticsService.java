package activitystatistics;

import activitystatistics.model.join.Enriched;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ActivityStatisticsService {

  private static final Logger log = LoggerFactory.getLogger(ActivityStatisticsService.class);

  private static final String STORE_NAME = "activity-statistics-store";

  private final HostInfo hostInfo;
  private final KafkaStreams streams;

  ActivityStatisticsService(HostInfo hostInfo, KafkaStreams streams) {
    this.hostInfo = hostInfo;
    this.streams = streams;
  }

  ReadOnlyKeyValueStore<String, ActivityStatistic> getStore() {
    return streams.store(
        StoreQueryParameters.fromNameAndType(STORE_NAME, QueryableStoreTypes.keyValueStore()));
  }

  void start() {
    Javalin app = Javalin.create().start(hostInfo.port());

    // Local key-value store query: all entries
    app.get("/activity-statistics", this::getAll);

    // Local key-value store query: approximate number of entries
    app.get("/activity-statistics/count", this::getCount);

    // Local key-value store query: approximate number of entries
    app.get("/activity-statistics/count/local", this::getCountLocal);

    // Local key-value store query: range scan (inclusive)
    app.get("/activity-statistics/:from/:to", this::getRange);

    // Local key-value store query: point-lookup / single-key lookup
    app.get("/activity-statistics/:key", this::getKey);
  }

  void getAll(Context ctx) {
    Map<String, List<Enriched>> activityStatistics = new HashMap<>();

    KeyValueIterator<String, ActivityStatistic> range = getStore().all();
    while (range.hasNext()) {
      KeyValue<String, ActivityStatistic> next = range.next();
      String activity = next.key;
      ActivityStatistic activityStatistic = next.value;
      activityStatistics.put(activity, activityStatistic.toList());
    }
    // close the iterator to avoid memory leaks!
    range.close();
    // return a JSON response
    ctx.json(activityStatistics);
  }

  void getRange(Context ctx) {
    String from = ctx.pathParam("from");
    String to = ctx.pathParam("to");

    Map<String, List<Enriched>> activityStatistics = new HashMap<>();

    KeyValueIterator<String, ActivityStatistic> range = getStore().range(from, to);
    while (range.hasNext()) {
      KeyValue<String, ActivityStatistic> next = range.next();
      String activity = next.key;
      ActivityStatistic activityStatistic = next.value;
      activityStatistics.put(activity, activityStatistic.toList());
    }
    // close the iterator to avoid memory leaks!
    range.close();
    // return a JSON response
    ctx.json(activityStatistics);
  }

  void getCount(Context ctx) {
    long count = getStore().approximateNumEntries();

    for (StreamsMetadata metadata : streams.allMetadataForStore(STORE_NAME)) {
      if (!hostInfo.equals(metadata.hostInfo())) {
        continue;
      }
      count += fetchCountFromRemoteInstance(metadata.hostInfo().host(), metadata.hostInfo().port());
    }

    ctx.json(count);
  }

  long fetchCountFromRemoteInstance(String host, int port) {
    OkHttpClient client = new OkHttpClient();

    String url = String.format("http://%s:%d/activity-statistics/count/local", host, port);
    Request request = new Request.Builder().url(url).build();

    try (Response response = client.newCall(request).execute()) {
      return Long.parseLong(response.body().string());
    } catch (Exception e) {
      // log error
      log.error("Could not get activity-statistics count", e);
      return 0L;
    }
  }

  void getCountLocal(Context ctx) {
    long count = 0L;
    try {
      count = getStore().approximateNumEntries();
    } catch (Exception e) {
      log.error("Could not get local activity-statistics count", e);
    } finally {
      ctx.result(String.valueOf(count));
    }
  }

  void getKey(Context ctx) {
    String productId = ctx.pathParam("key");

    // find out which host has the key
    KeyQueryMetadata metadata =
        streams.queryMetadataForKey(STORE_NAME, productId, Serdes.String().serializer());

    // the local instance has this key
    if (hostInfo.equals(metadata.activeHost())) {
      log.info("Querying local store for key");
      ActivityStatistic activityStatistic = getStore().get(productId);

      if (activityStatistic == null) {
        // activity was not found
        ctx.status(404);
        return;
      }

      // activity was found, so return the statistics
      ctx.json(activityStatistic.toList());
      return;
    }

    // a remote instance has the key
    String remoteHost = metadata.activeHost().host();
    int remotePort = metadata.activeHost().port();
    String url =
        String.format(
            "http://%s:%d/activity-statistics/%s",
            // params
            remoteHost, remotePort, productId);

    // issue the request
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).build();

    try (Response response = client.newCall(request).execute()) {
      log.info("Querying remote store for key");
      ctx.result(response.body().string());
    } catch (Exception e) {
      ctx.status(500);
    }
  }
}
