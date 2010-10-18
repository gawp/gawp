package com.metabroadcast.content;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Item;

import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.text.Truncator;


/**
 * Renders the 'simple' (non-relational) attributes of a {@link Item}
 * @author John Ayres (john@metabroadcast.com)
 */
public class SimpleItemAttributesModelBuilder implements ModelBuilder<Item> {
	
	private final Truncator truncator = new Truncator().withMaxLength(90).withOmissionMarker("...").onlyTruncateAtAWordBoundary().omitTrailingPunctuationWhenTruncated().onlyStartANewSentenceIfTheSentenceIsAtLeastPercentComplete(50);
    private final Truncator titleTruncator = new Truncator().withMaxLength(20).withOmissionMarker("...").onlyTruncateAtAWordBoundary().omitTrailingPunctuationWhenTruncated();
    private final ContentModelHelper modelHelper = new ContentModelHelper();
    
    private final Pattern seriesAndEpisodeTitlePattern = Pattern.compile("[Ss]eries \\d+.*[Ee]pisode \\d+");
    private final Pattern episodeTitlePattern = Pattern.compile("[Ee]pisode \\d+");
    
    
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
		model.put("description", truncator.truncatePossibleNull(item.getDescription()));
		model.put("curie", item.getCurie());
		model.put("externalUrl", item.getUri());
		model.put("seriesNumber", item.getSeriesNumber());
		model.put("episodeNumber", item.getEpisodeNumber());
		addPublisher(model, item);
		addTitles(model, item, brand);
		addBrandSummary(model, brand);
		
		modelHelper.addGenres(model, item);
		modelHelper.addChannel(model, item);
		return model;
	}
	
	public void addBrandSummary(SimpleModel model, BrandSummary brand) {
	    if (brand != null) {
	        SimpleModel brandModel = new SimpleModel();
	        brandModel.put("title", brand.getTitle());
	        brandModel.put("curie", brand.getCurie());
	        brandModel.put("uri", brand.getUri());
	        
	        model.put("brand", brandModel);
	    }
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
		model.put("titleTruncated", titleTruncator.truncatePossibleNull(item.getTitle()));
		
		Matcher seriesAndEpisodeMatcher = seriesAndEpisodeTitlePattern.matcher(item.getTitle());
		Matcher episodeMatcher = episodeTitlePattern.matcher(item.getTitle());
		model.put("titleIsEpisodeNumber", seriesAndEpisodeMatcher.matches() || episodeMatcher.matches());
	}

	public void addPublisher(SimpleModel model, Item item) {
		SimpleModel publisherModel = new SimpleModel();
		publisherModel.put("uri", item.getPublisher().getKey());
		publisherModel.put("name",  item.getPublisher().getName());
		model.put("publisher", publisherModel);
	}

}