package com.metabroadcast.content.www;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;

import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.TwitterUserDetails;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.user.www.UserModelHelper;

public class ConsumptionsModelHelper {
    private final ModelBuilder<Item> itemModelBuilder;
    private final UserModelHelper userHelper;
    private final ModelBuilder<Playlist> playlistModelBuilder;
    private final Log log = LogFactory.getLog(ConsumptionsModelHelper.class);

    public ConsumptionsModelHelper(ModelBuilder<Item> itemModelBuilder, ModelBuilder<Playlist> playlistModelBuilder, UserModelHelper userHelper) {
        this.itemModelBuilder = itemModelBuilder;
        this.playlistModelBuilder = playlistModelBuilder;
        this.userHelper = userHelper;
    }
    
    public SimpleModelList popularItemsModel(List<Count<TargetRef>> targetsByConsumes, Map<String, Description> itemMap) {
        SimpleModelList episodesModel = new SimpleModelList();
        
        for (Count<TargetRef> targetCount : targetsByConsumes) {
            SimpleModel episodeModel = new SimpleModel();
            episodeModel.put("count", targetCount.getCount());
            episodeModel.put("item", itemModelBuilder.build((Item)itemMap.get(targetCount.getTarget().ref())));
            episodesModel.add(episodeModel);
        }
        
        return episodesModel;
    }
    
    public SimpleModelList popularBrandsModel(List<Count<String>> brandsByConsumes, Map<String, Description> uriToBrandMap) {
        SimpleModelList brandsModel = new SimpleModelList();
        
        for (Count<String> brandCount : brandsByConsumes) {
            if (uriToBrandMap.containsKey(brandCount.getTarget())) {
                SimpleModel brandModel = new SimpleModel();
                brandModel.put("count", brandCount.getCount());
                brandModel.put("brand", playlistModelBuilder.build((Playlist) uriToBrandMap.get(brandCount.getTarget())));
                brandsModel.add(brandModel);
            }
            else {
                log.warn("Found consumption with brand not in content store: " + brandCount.getTarget());
            }
        }
        
        return brandsModel;
    }
    
    public SimpleModelList biggestConsumersModel(List<Count<UserRef>> usersByConsumes) {
        SimpleModelList consumersModel = new SimpleModelList();
        
        for (Count<UserRef> userCount : usersByConsumes) {
            SimpleModel consumerModel = new SimpleModel();
            consumerModel.put("count", userCount.getCount());
            consumerModel.put("user", userHelper.userDetailsModel((TwitterUserDetails) userHelper.getUserDetails(userCount.getTarget()).requireValue()));
            consumersModel.add(consumerModel);
        }
        
        return consumersModel;
    }
    
    public SimpleModelList buildRecentComsumersModelWithItems(List<Consumption> consumptions, Map<String, Description> uriToItemMap) {
        return recentConsumersModel(consumptions, uriToItemMap);
    }
    
    public SimpleModelList buildRecentConsumersModelWithoutItems(List<Consumption> consumptions) {
        return recentConsumersModel(consumptions, null);
    }
    
    private SimpleModelList recentConsumersModel(List<Consumption> consumptions, Map<String, Description> uriToItemMap) {
        SimpleModelList consumptionsModel = new SimpleModelList();
        
        Set<UserRef> processedConsumers = Sets.newHashSet();
        for (Consumption consumption : consumptions) {
            if (!processedConsumers.contains(consumption.userRef())) {
                processedConsumers.add(consumption.userRef());
                Item item = null;
                if (uriToItemMap != null) {
                    item = (Item) uriToItemMap.get(consumption.targetRef().ref());
                }
                SimpleModel consumptionModel = buildConsumptionModel(consumption, item);
                consumptionsModel.add(consumptionModel);
            }
        }
        
        return consumptionsModel;
    }
    
    private SimpleModel buildConsumptionModel(Consumption consumption, Item item) {
        SimpleModel consumptionModel = new SimpleModel();
        
        Maybe<UserDetails> userDetails = userHelper.getUserDetails(consumption.userRef());
        
        consumptionModel.put("user", userHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        if (item != null) {
            consumptionModel.put("item", itemModelBuilder.build(item));
        }
        consumptionModel.put("time", consumption.timestamp().toString("dd MMMM yy - hh:mm"));
        
        return consumptionModel;
    }
}
