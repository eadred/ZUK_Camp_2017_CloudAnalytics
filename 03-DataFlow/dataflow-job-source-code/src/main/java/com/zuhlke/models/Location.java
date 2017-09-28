package com.zuhlke.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Location {
    private double longitude;
    private double latitude;
    private boolean geolocated;

    public Location() {
        //Required for Jackson
    }

    public Location(
            double longitude,
            double latitude,
            boolean geolocated) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.geolocated = geolocated;
    }

    @JsonProperty
    public double getLongitude() { return longitude; }

    @JsonProperty
    public double getLatitude() { return latitude; }

    @JsonProperty
    public boolean getGeolocated() { return geolocated; }
}
