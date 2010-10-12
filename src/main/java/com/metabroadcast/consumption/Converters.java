package com.metabroadcast.consumption;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.ContentRefs;
import com.metabroadcast.content.SeriesOrder;

public class Converters {

    public static Maybe<Consumption> fromItem(UserRef userRef, Item item, Channel channel) {
        if (item == null) {
            return Maybe.nothing();
        }

        Maybe<Publisher> publisher = Publisher.fromKey(item.getPublisher().getKey());
        if (publisher.hasValue() && channel == null) {
            Channel c = Channel.onlineChannelForPublisher(publisher.requireValue());
            if (c != null) {
                channel = c;
            }
        }

        BrandSummary brand = item.getBrandSummary();
        TargetRef targetRef = new TargetRef(item.getUri(), ContentRefs.ITEM_DOMAIN);
        Set<String> genres = genres(item);

        return Maybe.just(new Consumption(userRef, targetRef, new DateTime(DateTimeZones.UTC), channel, publisher
                        .hasValue() ? publisher.requireValue().key() : null, brand != null ? brand.getUri() : null,
                        genres));
    }
    
    public static Maybe<Item> fromDescription(Description description) {
        Item item = null;
        
        if (description != null) {
            if (description instanceof Playlist) {
                Playlist playlist = (Playlist) description;
                List<Item> items = playlist.getItems();

                if (!items.isEmpty()) {
                    Collections.sort(items, new SeriesOrder());
                    Collections.reverse(items);
                    item = items.get(0);
                }
            } else if (description instanceof Item) {
                item = (Item) description;
            }
        }
        
        return Maybe.fromPossibleNullValue(item);
    }

    private static Set<String> genres(Item item) {
        Set<String> genres = Sets.newHashSet();
        for (String genre : item.getGenres()) {
            if (genre.startsWith("http://ref.atlasapi.org")) {
                genres.add(genre);
            }
        }
        return genres;
    }
}
