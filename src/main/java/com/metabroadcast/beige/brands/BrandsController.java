package com.metabroadcast.beige.brands;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Playlist;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
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
    

    public BrandsController(ContentStore contentStore, ConsumptionStore consumptionStore, UserProvider userProvider, UserModelHelper userHelper, ConsumedContentProvider consumedContentProvider) {
        this.contentStore = contentStore;
        this.consumptionStore = consumptionStore;
        this.userProvider = userProvider;
        this.userHelper = userHelper;
        this.consumedContentProvider = consumedContentProvider;
    }
    
    @RequestMapping(value = { "/brands/{brandCurie}" }, method = { RequestMethod.GET })
    public String getBrandPage(Map<String, Object> model, @PathVariable String brandCurie) {
        UserRef currentUserRef = userProvider.existingUser();
        
        Maybe<UserDetails> userDetails = userHelper.getUserDetails(currentUserRef);
        model.put("currentUserDetails", userHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        
        Maybe<Description> description = contentStore.resolve(brandCurie);
        Playlist playlist = (Playlist) description.requireValue();

        model.put("brand", brandDetailsModel(playlist));
        
        List<Consumption> consumptions = consumptionStore.recentConsumesOfBrand(playlist.getUri());
        
        List<Count<UserRef>> usersByConsumes = consumedContentProvider.findUserCounts(consumptions);
        Collections.sort(usersByConsumes);
        
        List<Count<TargetRef>> targetsByConsumes = consumedContentProvider.findTargetCounts(consumptions);
        Collections.sort(targetsByConsumes);
        
        
        model.put("consumers", consumersModel(consumptions));
        
        return "brands/brand";
    }
    
    private SimpleModelList consumersModel(List<Consumption> consumptions) {
        SimpleModelList consumersModel = new SimpleModelList();
        
        Set<UserRef> processedConsumers = Sets.newHashSet();
        for (Consumption consumption : consumptions) {
            if (!consumptions.contains(consumption.userRef())) {
                processedConsumers.add(consumption.userRef());
                consumersModel.add(recentConsumptionModel(consumption));
            }
        }
        
        
        for  (Consumption consumption : consumptions) {
            
        }
        
        return consumersModel;
    }
    
    private SimpleModel recentConsumptionModel(Consumption consumption) {
        SimpleModel consumptionModel = new SimpleModel();
        
        Maybe<UserDetails> userDetails = userHelper.getUserDetails(consumption.userRef());
        consumptionModel.put("user", userHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        consumptionModel.put("item", consumption.targetRef().toSimpleModel());
        consumptionModel.put("time", consumption.timestamp().toString("dd MMMM yy - hh:mm"));
        
        return consumptionModel;
    }
    
    private SimpleModel brandDetailsModel(Playlist brand) {
        SimpleModel brandModel = new SimpleModel();
        
        brandModel.put("title", brand.getTitle());
        brandModel.put("description", brand.getDescription());
        
        return brandModel;
    }
}
