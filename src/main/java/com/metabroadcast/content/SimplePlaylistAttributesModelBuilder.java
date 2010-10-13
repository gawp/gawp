package com.metabroadcast.content;

import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;

import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;

public class SimplePlaylistAttributesModelBuilder implements ModelBuilder<Playlist> {
    private ContentModelHelper modelHelper = new ContentModelHelper();

    @Override
    public SimpleModel build(Playlist list) {
        SimpleModel model = new SimpleModel();
        model.put("title", list.getTitle());
        model.put("curie", list.getCurie());
        model.put("uri", list.getUri());
        model.put("thumbnail", list.getThumbnail());
        model.put("image", list.getImage());
        model.put("description", list.getDescription());
        modelHelper.addGenres(model, list);
        modelHelper.addChannel(model, list);
        return model;
    }
    
    public static class PrimaryBrandModelBuilder implements ModelBuilder<Item> {
        
        private final ModelBuilder<BrandSummary> brandSummaryBuilder;

        public PrimaryBrandModelBuilder() {
            this(new SimpleBrandSummaryAttribuitesModelBuilder());
        }

        public PrimaryBrandModelBuilder(ModelBuilder<BrandSummary> brandSummaryBuilder) {
            this.brandSummaryBuilder = brandSummaryBuilder;
        }

        @Override
        public SimpleModel build(Item item) {
            SimpleModel model = new SimpleModel();
            if (item.getBrandSummary() != null) {
                model.put("brand", brandSummaryBuilder.build(item.getBrandSummary()));
            }
            return model;
        }
    }
    
    private static class SimpleBrandSummaryAttribuitesModelBuilder implements ModelBuilder<BrandSummary> {

		@Override
		public SimpleModel build(BrandSummary list) {
			SimpleModel model = new SimpleModel();
	        model.put("title", list.getTitle());
	        model.put("curie", list.getCurie());
	        model.put("uri", list.getUri());
	        model.put("description", list.getDescription());
		    return model;
		}
    	
    }
    
}
