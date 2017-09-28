package com.zuhlke.models.Streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Place {
    private String name;
    private String fullName;
    private String countryCode;
    private String placeType;
    private BoundingBox boundingBox;

    @JsonProperty
    public String getName() { return name; }

    @JsonProperty("full_name")
    public String getFullName() { return fullName; }

    @JsonProperty("country_code")
    public String getCountryCode() { return countryCode; }

    @JsonProperty("place_type")
    public String getPlaceType() { return placeType; }

    @JsonProperty("bounding_box")
    public BoundingBox getBoundingBox() { return boundingBox; }
}
