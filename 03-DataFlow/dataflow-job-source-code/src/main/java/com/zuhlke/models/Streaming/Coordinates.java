package com.zuhlke.models.Streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinates {
    private double lat;
    private double lon;

    public Coordinates() {}

    @JsonProperty
    public double getLat() { return lat; }

    @JsonProperty
    public double getLon() { return lon; }
}
