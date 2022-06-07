package activitystatistics.model.join;

import activitystatistics.model.Activity;
import activitystatistics.model.StatisticEvent;

public class StatisticWithActivity {
  private StatisticEvent statisticEvent;
  private Activity activity;

  public StatisticWithActivity(StatisticEvent statisticEvent, Activity activity) {
    this.statisticEvent = statisticEvent;
    this.activity = activity;
  }

  public StatisticEvent getStatisticEvent() {
    return this.statisticEvent;
  }

  public Activity getActivity() {
    return this.activity;
  }

  @Override
  public String toString() {
    return "{" + " statisticEvent='" + getStatisticEvent() + "'" + ", activity='" + getActivity() + "'" + "}";
  }
}
