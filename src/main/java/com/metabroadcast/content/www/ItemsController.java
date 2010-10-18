package com.metabroadcast.content.www;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.TwitterUserDetails;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.consumption.ConsumedContent;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.user.www.UserModelHelper;

@Controller
public class ItemsController {
    private final ContentStore contentStore;
    private final ConsumptionStore consumptionStore;
    private final UserProvider userProvider;
    private final UserModelHelper userHelper;
    private final ConsumedContentProvider consumedContentProvider;
    private final ModelBuilder<Item> itemModelBuilder;
    private final ConsumptionsModelHelper consumptionsModelHelper;

    public ItemsController(ContentStore contentStore, ConsumptionStore consumptionStore, UserProvider userProvider, UserModelHelper userHelper, 
                           ConsumedContentProvider consumedContentProvider, ModelBuilder<Item> itemModelBuilder, ConsumptionsModelHelper consumptionsModelHelper) {
        this.contentStore = contentStore;
        this.consumptionStore = consumptionStore;
        this.userProvider = userProvider;
        this.userHelper = userHelper;
        this.consumedContentProvider = consumedContentProvider;
        this.itemModelBuilder = itemModelBuilder;
        this.consumptionsModelHelper = consumptionsModelHelper;
    }
    
    @RequestMapping(value = { "/episodes/{itemCurie}" }, method = { RequestMethod.GET })
    public String getBrandPage(Map<String, Object> model, @PathVariable String itemCurie) {
        UserRef currentUserRef = userProvider.existingUser();
        
        Maybe<UserDetails> userDetails = userHelper.getUserDetails(currentUserRef);
        model.put("currentUserDetails", userHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        
        Maybe<Description> description = contentStore.resolve(itemCurie);
        Item item = (Item) description.requireValue();

        model.put("item", itemModelBuilder.build(item));
        
        /*List<ConsumedContent> recentConsumedContent = consumedContentProvider.findForBrand(playlist, MAX_RECENT_ITEMS);
        model.put("recentConsumptions", consumedContentModelListBuilder.build(recentConsumedContent));*/
        
        List<Consumption> consumptions = consumptionStore.recentConsumesOfItem(item.getUri());
        Set<String> targetUris = Sets.newHashSet();
        for (Consumption consumption : consumptions) {
            targetUris.add(consumption.targetRef().ref());
        }
        List<Count<UserRef>> usersByConsumes = consumedContentProvider.findUserCounts(consumptions);
        Collections.sort(usersByConsumes, Collections.reverseOrder());
        
        List<Count<TargetRef>> targetsByConsumes = consumedContentProvider.findTargetCounts(consumptions);
        Collections.sort(targetsByConsumes, Collections.reverseOrder());
        
        model.put("recentConsumes", consumptionsModelHelper.buildRecentConsumersModelWithoutItems(consumptions));
        
        return "items/item";
    }
}
