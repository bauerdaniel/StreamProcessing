package activitystatistics;

import activitystatistics.model.join.Enriched;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class ActivityStatistic {
  private final TreeSet<Enriched> statistics = new TreeSet<>();

  public ActivityStatistic add(final Enriched enriched) {
    statistics.add(enriched);
    return this;
  }

  public List<Enriched> toList() {

    Iterator<Enriched> statistics = this.statistics.iterator();
    List<Enriched> activityStatistics = new ArrayList<>();
    while (statistics.hasNext()) {
      activityStatistics.add(statistics.next());
    }

    return activityStatistics;
  }
}
