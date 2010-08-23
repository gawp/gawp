package com.metabroadcast.content;

import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;

import com.google.common.base.Function;
import com.metabroadcast.common.social.model.TargetRef;

public class ContentRefs {

    public static final String PLAYLIST_DOMAIN = "atlas-playlist";
    public static final String ITEM_DOMAIN = "atlas-item";
    public static final String GENRE_DOMAIN = "atlas-genre";
	
	public static Function<Item, TargetRef> ITEM_TO_REF = new Function<Item, TargetRef>() {

		@Override
		public TargetRef apply(Item item) {
			return new TargetRef(item.getUri(), ITEM_DOMAIN);
		}
		
	};
	
	public static Function<Playlist, TargetRef> PLAYLIST_TO_REF = new Function<Playlist, TargetRef>() {

		@Override
		public TargetRef apply(Playlist playlist) {
			return new TargetRef(playlist.getUri(), PLAYLIST_DOMAIN);
		}
		
	};

	public static TargetRef toPlaylistRef(String target) {
		return new TargetRef(target, PLAYLIST_DOMAIN);
	}
} 
