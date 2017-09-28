package com.zuhlke.models.Streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Hashtag {
    private String text;

    public Hashtag() {}

    @JsonProperty
    public String getText() { return text; }
}
