package com.zuhlke.models.Streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mention {
    private String screenName;

    public Mention() {}

    @JsonProperty("screen_name")
    public String getScreenName() { return screenName; }
}
