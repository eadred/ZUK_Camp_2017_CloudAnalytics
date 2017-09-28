package com.zuhlke.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Function;

public class Tweet {

    private String key;
    private long tweetid;
    private String content;
    private String usr;
    private String tstamp;
    private Location location;
    private String language;
    private String place;
    private int favourites;
    private float sentiment;

    public Tweet() {
        //Required for Jackson
    }

    public Tweet(
            String key,
            long tweetid,
            String content,
            String usr,
            String tstamp,
            Location location,
            String language,
            String place,
            int favourites,
            float sentiment) {

        this.key = key;
        this.tweetid = tweetid;
        this.content = content;
        this.usr = usr;
        this.tstamp = tstamp;
        this.location = location;
        this.language = language;
        this.place = place;
        this.favourites = favourites;
        this.sentiment = sentiment;
    }

    public static Tweet fromArray(String[] array) {
        return new Tweet(
                getPropertyFromArray(0, array),
                Tweet.<Long>getPropertyFromArray(1, array, s -> Long.parseLong(s), 0L),
                getPropertyFromArray(2, array),
                getPropertyFromArray(3, array),
                getPropertyFromArray(4, array),
                new Location(
                        Tweet.<Double>getPropertyFromArray(5, array, s -> Double.parseDouble(s), 0.0),
                        Tweet.<Double>getPropertyFromArray(6, array, s -> Double.parseDouble(s), 0.0),
                        Tweet.<Boolean>getPropertyFromArray(7, array, s -> Boolean.parseBoolean(s), false)
                ),
                getPropertyFromArray(8, array),
                getPropertyFromArray(9, array),
                Tweet.<Integer>getPropertyFromArray(10, array, s -> Integer.parseInt(s), 0),
                Tweet.<Float>getPropertyFromArray(11, array, s -> Float.parseFloat(s), 0.0F)
        );
    }

    private static <T> T getPropertyFromArray(int index, String[] parts, Function<String, T> convert, T defaultValue) {
        if (parts.length <= index) return defaultValue;

        String part = parts[index];
        if (part == null || part.length() == 0) return defaultValue;

        return convert.apply(part);
    }

    private static String getPropertyFromArray(int index, String[] array) {
        return getPropertyFromArray(index, array, s -> s, "");
    }

    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public long getTweetid() {
        return tweetid;
    }

    @JsonProperty
    public String getContent() {
        return content;
    }

    @JsonProperty
    public String getUsr() {
        return usr;
    }

    @JsonProperty
    public String getTstamp() {
        return tstamp;
    }

    @JsonProperty
    public Location getLocation() {
        return location;
    }

    @JsonProperty
    public String getLanguage() {
        return language;
    }

    @JsonProperty
    public String getPlace() {
        return place;
    }

    @JsonProperty
    public int getFavourites() {
        return favourites;
    }

    @JsonProperty
    public float getSentiment() {
        return sentiment;
    }
}
