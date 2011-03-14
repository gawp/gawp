package com.metabroadcast.content;


import static org.atlasapi.client.ScheduleQuery.builder;
import static org.atlasapi.client.query.AtlasQuery.filter;

import java.util.List;
import java.util.Map;

import org.atlasapi.client.AtlasClient;
import org.atlasapi.client.ScheduleQuery.ScheduleQueryBuilder;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.DiscoverQueryResult;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.media.entity.simple.ScheduleQueryResult;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.time.DateTimeZones;

@Component
public class AtlasContentStore implements ContentStore {

    private final AtlasClient client;
    private final static List<Publisher> PUBLISHERS = ImmutableList.of(Publisher.BBC, Publisher.C4, Publisher.ITV, Publisher.FIVE);

    @Autowired
    public AtlasContentStore(AtlasClient client) {
        this.client = client;
    }

    @Override
    public List<Item> getItemsByGenreUris(List<String> genreUris, Selection selection) {
        DiscoverQueryResult result = client.discover(filter().genres().in(genreUris).withSelection(selection));
        return ImmutableList.copyOf(Iterables.filter(result.getResults(), Item.class));
    }

    @Override
    public List<Item> getItemsByPlaylistUris(List<String> playlistUris, Selection selection) {
        ContentQueryResult content = client.content(playlistUris);
        
        ImmutableList.Builder<Item> items = ImmutableList.builder();
        for (Playlist playlist: Iterables.filter(content.getContents(), Playlist.class)) {
            items.addAll(Iterables.filter(playlist.getContent(), Item.class));
        }
        return selection.applyTo(items.build());
    }

    @Override
    public List<Playlist> getBrandsByPlaylistUri(String playlistUri, Selection selection) {
        ContentQueryResult content = client.content(ImmutableList.of(playlistUri));
        
        return selection.applyTo(Iterables.filter(content.getContents(), Playlist.class));
    }

    @Override
    public List<Item> getItemsOnNow(String channel) {
        DateTime now = new DateTime(DateTimeZones.UTC);
        ScheduleQueryBuilder query = builder().withChannels(Channel.fromUri(channel).requireValue()).withPublishers(PUBLISHERS).withOnBetween(new Interval(now, now));
        ScheduleQueryResult scheduleFor = client.scheduleFor(query.build());
        return Iterables.getOnlyElement(scheduleFor.getChannels()).getItems();
    }
   
	@Override
	public List<Item> itemsByUri(Iterable<String> itemUris) {
	    ContentQueryResult content = client.content(itemUris);
        
        return ImmutableList.copyOf(Iterables.filter(content.getContents(), Item.class));
	}

	@Override
	public Map<TargetRef, Playlist> resolvePlaylists(Iterable<TargetRef> refs) {
		if (!Iterables.all(refs, Predicates.compose(Predicates.equalTo(ContentRefs.PLAYLIST_DOMAIN), TargetRef.TO_DOMAIN))) {
			throw new UnsupportedOperationException("TODO: support genre lists and other lists");
		}
		ContentQueryResult content = client.content(Iterables.transform(refs, TargetRef.TO_REF));
		List<Playlist> playlists =  ImmutableList.copyOf(Iterables.filter(content.getContents(), Playlist.class));
		
		Builder<TargetRef, Playlist> map = ImmutableMap.builder();
		
		for (Playlist playlist : playlists) {
			map.put(ContentRefs.PLAYLIST_TO_REF.apply(playlist), playlist);
		}
		
		return map.build();
	}
  
    @Override
    public Maybe<Description> resolve(String curieOrUri) {
        try {
            ContentQueryResult content = client.content(ImmutableList.of(curieOrUri));
            List<Description> contents = content.getContents();
            if (contents.isEmpty()) {
                return Maybe.nothing();
            }
            return Maybe.fromPossibleNullValue(Iterables.getFirst(contents, null));
        } catch (Exception e) {
            return Maybe.nothing();
        }
    }

    @Override
    public Map<String, Description> resolveAll(Iterable<String> curiesOrUris) {
        ContentQueryResult content = client.content(curiesOrUris);
        return Maps.uniqueIndex(content.getContents(), new Function<Description, String>() {
            @Override
            public String apply(Description input) {
                return input.getUri();
            }
        });
    }
}
