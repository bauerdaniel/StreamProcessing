package activityapp;

import activityapp.model.Geolocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class GeolocationHistory {
    private final List<Geolocation> geolocations = new ArrayList<>();

    public GeolocationHistory add(final Geolocation geolocation) {
        geolocations.add(geolocation);
        return this;
    }

    public Geolocation first() {
        return geolocations.get(0);
    }

    public Geolocation last() {
        return geolocations.get(geolocations.size() - 1);
    }
}