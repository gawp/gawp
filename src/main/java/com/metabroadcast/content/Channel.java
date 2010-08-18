package com.metabroadcast.content;

public class Channel {

    private final String logo;
    private final String uri;
    private final String name;

    public Channel(String name, String uri, String logo) {
        this.name = name;
        this.uri = uri;
        this.logo = logo;
    }

    public String getLogo() {
        return logo;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }
}
