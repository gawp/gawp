package com.metabroadcast.content;


import static org.atlasapi.client.query.AtlasQuery.brands;
import static org.atlasapi.client.query.AtlasQuery.items;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.atlasapi.client.AtlasClient;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableMap.Builder;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.social.model.TargetRef;

@Component
public class AtlasContentStore implements ContentStore {

    private final AtlasClient client;

    @Autowired
    public AtlasContentStore(AtlasClient client) {
        this.client = client;
    }

    @Override
    public List<Item> getItemsByGenreUris(List<String> genreUris, Selection selection) {
        return client.query(items().itemGenres().in(genreUris).withSelection(selection));
    }

    @Override
    public List<Item> getItemsByPlaylistUris(List<String> playlistUris, Selection selection) {
        return client.query(items().playlistUri().in(playlistUris).withSelection(selection));
    }

    @Override
    public List<Playlist> getBrandsByPlaylistUri(String playlistUri, Selection selection) {
        return client.query(brands().playlistUri().equalTo(playlistUri));
    }

    @Override
    public List<Item> getItemsOnNow(String channel) {
        return client.query(items().transmissionTime().equalTo(new DateTime()).channel().equalTo(channel));
    }
   
	@Override
	public List<Item> itemsByUri(Iterable<String> itemUris) {
		return client.query(items().uri().in(itemUris));
	}

	@Override
	public Map<TargetRef, Playlist> resolvePlaylists(Iterable<TargetRef> refs) {
		if (!Iterables.all(refs, Predicates.compose(Predicates.equalTo(ContentRefs.PLAYLIST_DOMAIN), TargetRef.TO_DOMAIN))) {
			throw new UnsupportedOperationException("TODO: support genre lists and other lists");
		}
		List<Playlist> playlists = client.query(brands().uri().in(Iterables.transform(refs, TargetRef.TO_REF)));
		
		Builder<TargetRef, Playlist> map = ImmutableMap.builder();
		
		for (Playlist playlist : playlists) {
			map.put(ContentRefs.PLAYLIST_TO_REF.apply(playlist), playlist);
		}
		
		return map.build();
	}
  
    @Override
    public Maybe<Description> resolve(String curieOrUri) {
        try {
            Map<String, Description> anyQueryResults = client.any(Collections.singleton(curieOrUri));
            if (anyQueryResults.isEmpty()) {
                return Maybe.nothing();
            } else {
                return Maybe.just(Iterables.getOnlyElement(anyQueryResults.values()));
            }
        } catch (Exception e) {
            return Maybe.nothing();
        }
    }

    @Override
    public Map<String, Description> resolveAll(Iterable<String> curiesOrUris) {
        return client.any(curiesOrUris);
    }
}
