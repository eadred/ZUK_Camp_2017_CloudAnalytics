package com.zuhlke.models.Streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BoundingBox {
    private String type;
    private Coordinates[] coordinates;

    public BoundingBox() {}

    @JsonProperty
    public String getType() { return type; }

    @JsonProperty
    public Coordinates[] getCoordinates() { return coordinates; }
}
