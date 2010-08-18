package com.metabroadcast.content;

import java.util.Comparator;

import org.atlasapi.media.entity.simple.Item;

public class SeriesOrder implements Comparator<Item> {

	@Override
	public int compare(Item item1, Item item2) {
		if (item1.getSeriesNumber() == null && item2.getSeriesNumber() == null) {
			 return defaultOrder(item1, item2);
		}
		if (item1.getSeriesNumber() == null) {
			return 1;
		}
		if (item2.getSeriesNumber() == null) {
			return -1;
		}
		int seriesComparison = item1.getSeriesNumber().compareTo(item2.getSeriesNumber());
		if (seriesComparison != 0) {
			return seriesComparison;
		}
		return compareEpisodeNumbers(item1, item2);
		
	}

	private int compareEpisodeNumbers(Item item1, Item item2) {
		if (item1.getEpisodeNumber() == null && item2.getEpisodeNumber() == null) {
			 return defaultOrder(item1, item2);
		}
		if (item1.getEpisodeNumber() == null) {
			return 1;
		}
		if (item2.getEpisodeNumber() == null) {
			return -1;
		}
		int episodeComparison = item1.getEpisodeNumber().compareTo(item2.getEpisodeNumber());
		if (episodeComparison != 0) {
			return episodeComparison;
		}
		return defaultOrder(item1, item2);
	}

	private int defaultOrder(Item item1, Item item2) {
		return item1.getUri().compareTo(item2.getUri());
	}
}
