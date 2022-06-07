package activitystatistics.model;

public class StatisticEvent {
  private Long userId;
  private Long activityId;
  private Double viewDuration;

  public Long getUserId() {
    return this.userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getActivityId() {
    return this.activityId;
  }

  public void setActivityId(Long activityId) {
    this.activityId = activityId;
  }

  public Double getViewDuration() {
    return this.viewDuration;
  }

  public void setViewDuration(Double viewDuration) {
    this.viewDuration = viewDuration;
  }
}
