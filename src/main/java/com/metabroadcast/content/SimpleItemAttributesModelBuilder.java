package com.metabroadcast.content;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Item;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;


/**
 * Renders the 'simple' (non-relational) attributes of a {@link Item}
 * @author John Ayres (john@metabroadcast.com)
 */
public class SimpleItemAttributesModelBuilder implements ModelBuilder<Item> {
	
	private static final int TRUNCATE_TITLES_TO = 30;

	private Set<String> allowedGenrePrefixes = Sets.newHashSet("http://ref.atlasapi.org/genres/atlas/");
	
	public SimpleModel build(Item item) {
		SimpleModel model = new SimpleModel();
		BrandSummary brand = item.getBrandSummary();
		model.put("uri", item.getUri());
		model.put("thumbnail", item.getThumbnail());
		if (item.getImage() != null) {
			model.put("image", item.getImage());
		} else if (item.getThumbnail() != null) {
			model.put("image", item.getThumbnail());
		}
		model.put("description", item.getDescription());
		model.put("curie", item.getCurie());
		model.put("externalUrl", item.getUri());
		addPublisher(model, item);
		addTitles(model, item, brand);
		addGenres(model, item);
		return model;
	}
	
	private void addGenres(SimpleModel model, Item item) {
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
	
	public static String displayName(String publisher) {
		if (publisher == null) { 
			return "";
		}
		if ("bbc.co.uk".equals(publisher)) {
			return "BBC iPlayer";
		}
		if ("channel4.com".equals(publisher)) {
			return "Channel 4 - 4OD";
		}
		if ("youtube".equals(publisher)) {
			return "YouTube";
		}
		if ("ted.com".equals(publisher)) {
			return "TED Talks";
		}
		if ("http://vimeo.com/".equals(publisher)) {
			return "Vimeo";
		}
		return "";
		
	}
	
	public void addTitles(SimpleModel model, Item item, BrandSummary list) {
		if (list == null || list.getTitle() == null) {
			model.put("primaryTitle", item.getTitle());
		} else {
			model.put("primaryTitle", list.getTitle());
			model.put("secondaryTitle", item.getTitle());
		}
		model.put("title", item.getTitle());
		model.put("titleTruncated", truncate(item.getTitle()));
	}
	
	private String truncate(String title) {
		if (title == null) {
			return null;
		}
		if (title.length() <= TRUNCATE_TITLES_TO) {
			return title;
		} else {
			String ellipsis = "...";
			return title.substring(0, TRUNCATE_TITLES_TO - ellipsis.length()) + ellipsis;
		}
	}

	public void addPublisher(SimpleModel model, Item item) {
		SimpleModel publisherModel = new SimpleModel();
		publisherModel.put("uri", item.getPublisher().getKey());
		publisherModel.put("name",  item.getPublisher().getName());
		model.put("publisher", publisherModel);
	}

}