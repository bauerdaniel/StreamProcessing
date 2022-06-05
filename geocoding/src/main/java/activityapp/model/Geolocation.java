package activityapp.model;

public class Geolocation implements TimeEvent {

    private String timestamp;

    private double latitude;
    private double longitude;

    public String getTimestamp() {
        return this.timestamp;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public String toString() {
        return "Geolocation{" +
                "timestamp='" + timestamp + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
