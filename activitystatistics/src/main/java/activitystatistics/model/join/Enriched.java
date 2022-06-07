package activitystatistics.model.join;

import activitystatistics.model.User;

public class Enriched implements Comparable<Enriched> {
  private Long activityId;
  private Long userId;
  private String activityName;
  private String userName;
  private Double viewDuration;

  public Enriched(StatisticWithActivity statisticWithActivity, User user) {
    this.activityId = statisticWithActivity.getActivity().getId();
    this.userId = user.getId();
    this.activityName = statisticWithActivity.getActivity().getName();
    this.userName = user.getName();
    this.viewDuration = statisticWithActivity.getStatisticEvent().getViewDuration();
  }

  @Override
  public int compareTo(Enriched o) {
    return Double.compare(o.viewDuration, viewDuration);
  }

  public Long getActivityId() {
    return this.activityId;
  }

  public Long getUserId() {
    return this.userId;
  }

  public String getActivityName() {
    return this.activityName;
  }

  public String getUserName() {
    return this.userName;
  }

  public Double getViewDuration() {
    return this.viewDuration;
  }

  @Override
  public String toString() {
    return "{"
        + " activityId='"
        + getActivityId()
        + "'"
        + ", activityName='"
        + getActivityName()
        + "'"
        + ", userName='"
        + getUserName()
        + "'"
        + ", viewDuration='"
        + getViewDuration()
        + "'"
        + "}";
  }
}
