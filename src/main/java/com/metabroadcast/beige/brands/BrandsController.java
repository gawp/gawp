package com.metabroadcast.beige.brands;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.social.model.TargetRef;
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
public class BrandsController {
    private final ContentStore contentStore;
    private final ConsumptionStore consumptionStore;
    private final UserModelHelper userHelper;
    private final UserProvider userProvider;
    private final ConsumedContentProvider consumedContentProvider;
    private final ModelBuilder<Item> itemModelBuilder;
    private final ModelBuilder<Playlist> playlistModelBuilder;
    

    public BrandsController(ContentStore contentStore, ConsumptionStore consumptionStore, UserProvider userProvider, UserModelHelper userHelper, ConsumedContentProvider consumedContentProvider, ModelBuilder<Playlist> playlistModelBuilder, ModelBuilder<Item> itemModelBuilder) {
        this.contentStore = contentStore;
        this.consumptionStore = consumptionStore;
        this.userProvider = userProvider;
        this.userHelper = userHelper;
        this.consumedContentProvider = consumedContentProvider;
        this.playlistModelBuilder = playlistModelBuilder;
        this.itemModelBuilder = itemModelBuilder;
    }
    
    @RequestMapping(value = { "/brands/{brandCurie}" }, method = { RequestMethod.GET })
    public String getBrandPage(Map<String, Object> model, @PathVariable String brandCurie) {
        UserRef currentUserRef = userProvider.existingUser();
        
        Maybe<UserDetails> userDetails = userHelper.getUserDetails(currentUserRef);
        model.put("currentUserDetails", userHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        
        Maybe<Description> description = contentStore.resolve(brandCurie);
        Playlist playlist = (Playlist) description.requireValue();

        model.put("brand", playlistModelBuilder.build(playlist));
        List<Consumption> consumptions = consumptionStore.recentConsumesOfBrand(playlist.getUri());
        Set<String> targetUris = Sets.newHashSet();
        for (Consumption consumption : consumptions) {
            targetUris.add(consumption.targetRef().ref());
        }
        Map<String, Description> itemMap = contentStore.resolveAll(targetUris);
        
        List<Count<UserRef>> usersByConsumes = consumedContentProvider.findUserCounts(consumptions);
        Collections.sort(usersByConsumes, Collections.reverseOrder());
        
        List<Count<TargetRef>> targetsByConsumes = consumedContentProvider.findTargetCounts(consumptions);
        Collections.sort(targetsByConsumes, Collections.reverseOrder());
        
        model.put("recentConsumes", recentConsumersModel(consumptions, itemMap));
        model.put("biggestConsumers", biggestConsumersModel(usersByConsumes));
        model.put("popularItems", popularItemsModel(targetsByConsumes, itemMap));
        
        return "brands/brand";
    }
    
    private SimpleModelList popularItemsModel(List<Count<TargetRef>> targetsByConsumes, Map<String, Description> itemMap) {
        SimpleModelList episodesModel = new SimpleModelList();
        
        for (Count<TargetRef> targetCount : targetsByConsumes) {
            SimpleModel episodeModel = new SimpleModel();
            episodeModel.put("count", targetCount.getCount());
            episodeModel.put("item", itemModelBuilder.build((Item)itemMap.get(targetCount.getTarget().ref())));
            episodesModel.add(episodeModel);
        }
        
        return episodesModel;
    }
    
    private SimpleModelList biggestConsumersModel(List<Count<UserRef>> usersByConsumes) {
        SimpleModelList consumersModel = new SimpleModelList();
        
        for (Count<UserRef> userCount : usersByConsumes) {
            SimpleModel consumerModel = new SimpleModel();
            consumerModel.put("count", userCount.getCount());
            consumerModel.put("user", userHelper.userDetailsModel((TwitterUserDetails) userHelper.getUserDetails(userCount.getTarget()).requireValue()));
            consumersModel.add(consumerModel);
        }
        
        return consumersModel;
    }
    
    private SimpleModelList recentConsumersModel(List<Consumption> consumptions, Map<String, Description> itemMap) {
        SimpleModelList consumptionsModel = new SimpleModelList();
        
        Set<UserRef> processedConsumers = Sets.newHashSet();
        for (Consumption consumption : consumptions) {
            if (!processedConsumers.contains(consumption.userRef())) {
                processedConsumers.add(consumption.userRef());
                consumptionsModel.add(consumptionModel(consumption, itemMap));
            }
        }
        
        return consumptionsModel;
    }
    
    private SimpleModel consumptionModel(Consumption consumption, Map<String, Description> itemMap) {
        SimpleModel consumptionModel = new SimpleModel();
        
        Maybe<UserDetails> userDetails = userHelper.getUserDetails(consumption.userRef());
        
        consumptionModel.put("user", userHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        consumptionModel.put("item", itemModelBuilder.build((Item) itemMap.get(consumption.targetRef().ref())));
        consumptionModel.put("time", consumption.timestamp().toString("dd MMMM yy - hh:mm"));
        
        return consumptionModel;
    }
    
}
