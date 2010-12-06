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
import com.metabroadcast.consumption.www.ConsumedContentModelBuilder;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.user.www.UserModelHelper;

public class ConsumptionsModelHelper {
    private final ModelBuilder<Item> itemModelBuilder;
    private final UserModelHelper userHelper;
    private final ModelBuilder<Playlist> playlistModelBuilder;
    private final Log log = LogFactory.getLog(ConsumptionsModelHelper.class);
    private final ContentStore contentStore;

    public ConsumptionsModelHelper(ContentStore contentStore, ModelBuilder<Item> itemModelBuilder, ModelBuilder<Playlist> playlistModelBuilder, UserModelHelper userHelper) {
        this.contentStore = contentStore;
        this.itemModelBuilder = itemModelBuilder;
        this.playlistModelBuilder = playlistModelBuilder;
        this.userHelper = userHelper;
    }
    
    public SimpleModelList popularItemsModel(List<Count<TargetRef>> targetsByConsumes, Map<String, Description> itemMap) {
        SimpleModelList episodesModel = new SimpleModelList();
        
        for (Count<TargetRef> targetCount : targetsByConsumes) {
            Item item = (Item) itemMap.get(targetCount.getTarget().ref());
            if (item != null) {
                SimpleModel episodeModel = new SimpleModel();
                episodeModel.putAsString("count", targetCount.getCount());
                episodeModel.put("target", itemModelBuilder.build(item));
                episodesModel.add(episodeModel);
            }
        }
        
        return episodesModel;
    }
    
    public SimpleModelList popularBrandsModel(List<Count<String>> brandsByConsumes) {
        SimpleModelList brandsModel = new SimpleModelList();
        
        for (Count<String> brandCount : brandsByConsumes) {
            Maybe<Description> desc = contentStore.resolve(brandCount.getTarget());
            if (desc.hasValue()) {
                SimpleModel brandModel = new SimpleModel();
                brandModel.putAsString("count", brandCount.getCount());
                brandModel.put("target", playlistModelBuilder.build((Playlist) desc.requireValue()));
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
            consumerModel.putAsString("count", userCount.getCount());
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
                SimpleModel consumptionModel = null;
                
                if (uriToItemMap != null) {
                    if (uriToItemMap.containsKey(consumption.targetRef().ref())) {
                        Item item = (Item) uriToItemMap.get(consumption.targetRef().ref());
                        consumptionModel = buildConsumptionModel(consumption, item);
                    }
                    else {
                        log.debug("found consumption with item which is not in atlas: " + consumption.targetRef().ref());
                    }
                }
                else {
                    consumptionModel = buildConsumptionModel(consumption, null);
                }
                
                if (consumptionModel != null) {
                    consumptionsModel.add(consumptionModel);
                    processedConsumers.add(consumption.userRef());
                }
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
        consumptionModel.put("ago", ConsumedContentModelBuilder.ago(consumption.timestamp()));
        
        return consumptionModel;
    }
}
