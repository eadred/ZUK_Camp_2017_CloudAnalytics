package com.zuhlke.models.Streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StreamedTweet {
    private String usr;
    private String tstamp;
    private String tweet;
    private Hashtag[] hashtags;
    private Mention[] mentions;
    private Location location;
    private String lang;
    private Place place;

    public StreamedTweet() {}

    @JsonProperty
    public String getUsr() { return usr; }

    @JsonProperty
    public String getTstamp() { return tstamp; }

    @JsonProperty
    public String getTweet() { return tweet; }

    @JsonProperty
    public Hashtag[] getHashtags() { return hashtags; }

    @JsonProperty
    public Mention[] getMentions() { return mentions; }

    @JsonProperty
    public Location getLocation() { return location; }

    @JsonProperty
    public String getLang() { return lang; }

    @JsonProperty
    public Place getPlace() { return place; }
}
