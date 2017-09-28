package com.zuhlke.models.Streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Location {
    private Boolean geolocated;
    private Coordinates coordinates;

    public Location() {}

    @JsonProperty
    public Boolean getGeolocated() { return geolocated; }

    @JsonProperty
    public Coordinates getCoordinates() { return coordinates; }
}
