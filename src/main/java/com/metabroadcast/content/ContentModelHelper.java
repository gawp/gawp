package com.metabroadcast.content;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.Description;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.SimpleModel;

public class ContentModelHelper {
    private Set<String> allowedGenrePrefixes = Sets.newHashSet("http://ref.atlasapi.org/genres/atlas/");
    
    public void addGenres(SimpleModel model, Description item) {
        List<SimpleModel> genres = Lists.newArrayList();
        for (String genreUri : item.getGenres()) {
            for (String prefix : allowedGenrePrefixes) {
                if (genreUri.startsWith(prefix)) {
                    SimpleModel genreModel = new SimpleModel();
                    genreModel.put("name", genreUri.substring(prefix.length()));
                    genreModel.put("uri", genreUri);
                    genres.add(genreModel);
                    break;
                }
            }
        }
        model.put("genres", genres);        
    }
    
    public void addChannel(SimpleModel model, Description item) {
        Maybe<Publisher> publisher = Publisher.fromKey(item.getPublisher().getKey());
        if (publisher.hasValue()) {
            model.put("channel", Channel.onlineChannelForPublisher(publisher.requireValue()).toModel());
        }
    }
}
