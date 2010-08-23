package com.metabroadcast.content;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.social.model.TargetRef;

public interface ContentStore {

    List<Item> getItemsByPlaylistUris(List<String> playlistUris, Selection selection);

    List<Item> itemsByUri(Iterable<String> itemUris);
    
    List<Item> getItemsByGenreUris(List<String> genreUris, Selection selection);
    
    Map<TargetRef, Playlist> resolvePlaylists(Iterable<TargetRef> refs);
    
    Maybe<Description> resolve(String curieOrUri);
    
    Map<String, Description> resolveAll(Iterable<String> curiesOrUris);
    
    List<Playlist> getBrandsByPlaylistUri(String playlistUri, Selection selection);

    List<Item> getItemOnNow(String channel);
}
