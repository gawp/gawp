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
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.social.model.TwitterUserDetails;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.user.www.UserModelHelper;

@Controller
public class GenreController {
    private final ConsumptionStore consumptionStore;
    
    private final static String GENRE_PREFIX = "http://ref.atlasapi.org/genres/atlas/";

    private final ContentStore contentStore;
    private final ConsumptionsModelHelper consumptionModelHelper;
    private final ConsumedContentProvider consumedContentProvider;
    private final UserModelHelper userModelHelper;
    private final UserProvider userProvider;

    public GenreController(ConsumptionStore consumptionStore, ContentStore contentStore, ConsumptionsModelHelper consumptionModelHelper, ConsumedContentProvider consumedContentProvider, UserModelHelper userModelHelper, UserProvider userProvider) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
        this.consumptionModelHelper = consumptionModelHelper;
        this.consumedContentProvider = consumedContentProvider;
        this.userModelHelper = userModelHelper;
        this.userProvider = userProvider;
    }
    
    @RequestMapping(value = { "/genres/{genreName}" }, method = { RequestMethod.GET })
    public String getChannelPage(Map<String, Object> model, @PathVariable String genreName) {
        
        UserRef currentUserRef = userProvider.existingUser();
        
        Maybe<UserDetails> userDetails = userModelHelper.getUserDetails(currentUserRef);
        model.put("currentUserDetails", userModelHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        
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
        
        model.put("popularBrands", consumptionModelHelper.popularBrandsModel(brandCounts));
        
        return "genres/genre";
    }
}
