package com.metabroadcast.content.www;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.simple.Description;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.ContentStore;

@Controller
public class GenreController {
    private final ConsumptionStore consumptionStore;
    
    private final static String GENRE_PREFIX = "http://ref.atlasapi.org/genres/atlas/";

    private final ContentStore contentStore;

    private final ConsumptionsModelHelper consumptionModelHelper;

    private final ConsumedContentProvider consumedContentProvider;

    public GenreController(ConsumptionStore consumptionStore, ContentStore contentStore, ConsumptionsModelHelper consumptionModelHelper, ConsumedContentProvider consumedContentProvider) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
        this.consumptionModelHelper = consumptionModelHelper;
        this.consumedContentProvider = consumedContentProvider;
    }
    
    @RequestMapping(value = { "/genres/{genreName}" }, method = { RequestMethod.GET })
    public String getChannelPage(Map<String, Object> model, @PathVariable String genreName) {
        
        String genreUri = GENRE_PREFIX + genreName;
        
        SimpleModel genreModel = new SimpleModel();
        genreModel.put("name", genreName);
        genreModel.put("uri", genreUri);
        model.put("genre", genreModel);
        
        List<Consumption> recentConsumptions = consumptionStore.recentConsumesOfGenre(genreUri);
        
        Set<String> itemUris = Sets.newHashSet();
        for (Consumption consumption : recentConsumptions) {
            itemUris.add(consumption.targetRef().ref());
        }
        
        Map<String, Description> uriToItemMap = contentStore.resolveAll(itemUris);
        model.put("recentConsumes", consumptionModelHelper.buildRecentComsumersModelWithItems(recentConsumptions, uriToItemMap));
        
        List<Count<String>> brandCounts = consumedContentProvider.findBrandCounts(recentConsumptions);
        Set<String> brandUris = Sets.newHashSet();
        for (Count<String> brandCount : brandCounts) {
            brandUris.add(brandCount.getTarget());
        }
        
        Map<String, Description> uriToBrandMap = contentStore.resolveAll(brandUris);
        model.put("popularBrands", consumptionModelHelper.popularBrandsModel(brandCounts, uriToBrandMap));
        
        return "genres/genre";
    }
}
