package com.metabroadcast.content;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.model.SimpleModel;

public enum Channel {

    BBC_IPLAYER("iPlayer", "http://www.bbc.co.uk/iplayer", "/images/bbc_iplayer.png"),
    HULU("Hulu", "http://www.hulu.com", "/images/hulu.png"),
    C4_4OD("4oD", "http://www.channel4.com", "/images/c4_4od.png"),
    BBC_ONE("BBC One", "http://www.bbc.co.uk/services/bbcone/london", "/images/bbc_one.png"), 
    BBC_TWO("BBC Two", "http://www.bbc.co.uk/services/bbctwo/england", "/images/bbc_two.png"),
    BBC_NEWS("BBC News", "http://www.bbc.co.uk/services/bbcnews", "/images/bbc_news.png"),
    BBC_PARLIMENT("BBC Parliment", "http://www.bbc.co.uk/services/parliament", "/images/bbc_parliment.png");
    
    private final String logo;
    private final String uri;
    private final String name;

    Channel(String name, String uri, String logo) {
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
    
    public SimpleModel toModel() {
        SimpleModel model = new SimpleModel();
        model.put("name", name);
        model.put("uri", uri);
        model.put("logo", logo);
        return model;
    }
    
    public static Channel fromUri(String uri) {
        for (Channel channel: values()) {
            if (channel.uri.equals(uri)) {
                return channel;
            }
        }
        return null;
    }
    
    public static List<Map<String, String>> mapList() {
        List<Map<String, String>> channelList = Lists.newArrayList();
        for (Channel channel: values()) {
            Map<String, String> channelMap = Maps.newHashMap();
            channelMap.put("name", channel.getName());
            channelMap.put("uri", channel.getUri());
            channelMap.put("logo", channel.getLogo());
            channelList.add(channelMap);
        }
        return channelList;
    }
    
    public static Channel onlineChannelForPublisher(Publisher publisher) {
        if (publisher.equals(Publisher.BBC)) {
            return Channel.BBC_IPLAYER;
        }
        if (publisher.equals(Publisher.HULU)) {
            return Channel.HULU;
        }
        if (publisher.equals(Publisher.C4)) {
            return Channel.C4_4OD;
        }
        return null;
    }
}
