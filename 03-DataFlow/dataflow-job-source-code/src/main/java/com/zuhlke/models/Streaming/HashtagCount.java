package com.zuhlke.models.Streaming;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.joda.time.DateTime;
import org.joda.time.Instant;

@DefaultCoder(AvroCoder.class)
public class HashtagCount {
    private String hashtag;
    private Long count;
    private Instant timestamp;

    public HashtagCount() {

    }

    public HashtagCount(String hashtag, Long count, Instant timestamp) {
        this.hashtag = hashtag;
        this.count = count;
        this.timestamp = timestamp;
    }

    public String getHashtag() { return hashtag; }

    public Long getCount() { return count; }

    public Instant getTimestamp() { return timestamp; }
}
