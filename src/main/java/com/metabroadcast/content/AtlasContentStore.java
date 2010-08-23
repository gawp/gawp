package com.metabroadcast.content;

import static org.atlasapi.content.criteria.ContentQueryBuilder.query;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.atlasapi.client.AtlasClient;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.time.DateTimeZones;

@Component
public class AtlasContentStore implements ContentStore {
    
    private final AtlasClient client;
    
    @Autowired
    public AtlasContentStore(AtlasClient client) {
        this.client = client;
    }

    @Override
    public List<Item> getItemsByGenreUris(List<String> genreUris, Selection selection) {
        return performItemQuery(query().equalTo(Attributes.ITEM_GENRE, genreUris).withSelection(selection));
    }

    @Override
    public List<Item> getItemsByPlaylistUris(List<String> playlistUris, Selection selection) {
        return performItemQuery(query().equalTo(Attributes.PLAYLIST_URI, playlistUris).withSelection(selection));
    }
    
    @Override
    public List<Playlist> getBrandsByPlaylistUri(String playlistUri, Selection selection) {
        return performBrandQuery(query().equalTo(Attributes.PLAYLIST_URI, playlistUri));//.withSelection(selection));
    }
    
    @Override
    public List<Item> getItemOnNow(String channel) {
        return performItemQuery(query().after(Attributes.BROADCAST_TRANSMISSION_TIME, new DateTime(DateTimeZones.UTC).minusMinutes(30)).before(Attributes.BROADCAST_TRANSMISSION_TIME, new DateTime(DateTimeZones.UTC).plusMinutes(30)).equalTo(Attributes.BROADCAST_ON, channel));
    }
    
    private List<Item> performItemQuery(ContentQueryBuilder query) {
        return client.items(query.build());
    }
    
    private List<Playlist> performBrandQuery(ContentQueryBuilder query) {
        return client.brands(query.build());
    }

   
	@Override
	public List<Item> itemsByUri(Iterable<String> itemUris) {
		return performItemQuery(query().equalTo(Attributes.ITEM_URI, Lists.newArrayList(itemUris)));
	}

	@Override
	public Map<TargetRef, Playlist> resolvePlaylists(Iterable<TargetRef> refs) {
		if (!Iterables.all(refs, Predicates.compose(Predicates.equalTo(ContentRefs.PLAYLIST_DOMAIN), TargetRef.TO_DOMAIN))) {
			throw new UnsupportedOperationException("TODO: support genre lists and other lists");
		}
		List<Playlist> playlists = performBrandQuery(query().equalTo(Attributes.BRAND_URI, Iterables.transform(refs, TargetRef.TO_REF)));
		
		Builder<TargetRef, Playlist> map = ImmutableMap.builder();
		
		for (Playlist playlist : playlists) {
			map.put(ContentRefs.PLAYLIST_TO_REF.apply(playlist), playlist);
		}
		
		return map.build();
	}

	@Override
	public Maybe<Description> resolve(String curieOrUri) {
		Map<String, Description> anyQueryResults = client.any(Collections.singleton(curieOrUri));
		if (anyQueryResults.isEmpty()) {
			return Maybe.nothing();
		}
		else {
			return Maybe.just(Iterables.getOnlyElement(anyQueryResults.values()));
		}
	}

	@Override
	public Map<String, Description> resolveAll(Iterable<String> curiesOrUris) {
		return client.any(curiesOrUris);
	}
}
