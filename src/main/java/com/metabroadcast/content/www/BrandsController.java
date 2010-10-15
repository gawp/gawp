package com.metabroadcast.content.www;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Playlist;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.TwitterUserDetails;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.consumption.ConsumedContent;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.consumption.www.ConsumedContentModelBuilder;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.user.www.UserModelHelper;

@Controller
public class BrandsController {
    private final static int MAX_RECENT_ITEMS = 10;
    private final static int MAX_TOP_ITEMS = 8;
    
    private final ContentStore contentStore;
    private final ConsumptionStore consumptionStore;
    private final UserModelHelper userHelper;
    private final UserProvider userProvider;
    private final ConsumedContentProvider consumedContentProvider;
    private final ModelBuilder<Playlist> playlistModelBuilder;
    private final ConsumptionsModelHelper consumptionsModelHelper;
    
    private final ModelListBuilder<ConsumedContent> consumedContentModelListBuilder;
    

    public BrandsController(ContentStore contentStore, ConsumptionStore consumptionStore, UserProvider userProvider, UserModelHelper userHelper, ConsumedContentProvider consumedContentProvider, 
                            ModelBuilder<Playlist> playlistModelBuilder, ConsumptionsModelHelper consumptionsModelHelper) {
        this.contentStore = contentStore;
        this.consumptionStore = consumptionStore;
        this.userProvider = userProvider;
        this.userHelper = userHelper;
        this.consumedContentProvider = consumedContentProvider;
        this.playlistModelBuilder = playlistModelBuilder;
        this.consumptionsModelHelper = consumptionsModelHelper;
        this.consumedContentModelListBuilder = DelegatingModelListBuilder.delegateTo(
                new ConsumedContentModelBuilder(new SimplePlaylistAttributesModelBuilder(), new SimpleItemAttributesModelBuilder(), userHelper));
    }
    
    @RequestMapping(value = { "/shows/{brandCurie}" }, method = { RequestMethod.GET })
    public String getBrandPage(Map<String, Object> model, @PathVariable String brandCurie) {
        UserRef currentUserRef = userProvider.existingUser();
        
        Maybe<UserDetails> userDetails = userHelper.getUserDetails(currentUserRef);
        model.put("currentUserDetails", userHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        
        Maybe<Description> description = contentStore.resolve(brandCurie);
        Playlist playlist = (Playlist) description.requireValue();

        model.put("brand", playlistModelBuilder.build(playlist));
        List<ConsumedContent> recentConsumedContent = consumedContentProvider.findForBrand(playlist, MAX_RECENT_ITEMS);
        model.put("recentConsumptions", consumedContentModelListBuilder.build(recentConsumedContent));
        
        List<Consumption> consumptions = consumptionStore.recentConsumesOfBrand(playlist.getUri(), new DateTime(DateTimeZones.UTC).minusWeeks(4));
        Set<String> targetUris = Sets.newHashSet();
        for (Consumption consumption : consumptions) {
            targetUris.add(consumption.targetRef().ref());
        }
        Map<String, Description> itemMap = contentStore.resolveAll(targetUris);
        
        List<Count<UserRef>> usersByConsumes = consumedContentProvider.findUserCounts(consumptions);
        Collections.sort(usersByConsumes, Collections.reverseOrder());
        
        List<Count<TargetRef>> targetsByConsumes = consumedContentProvider.findTargetCounts(consumptions);
        Collections.sort(targetsByConsumes, Collections.reverseOrder());
        
        model.put("biggestConsumers", consumptionsModelHelper.biggestConsumersModel(usersByConsumes));
        model.put("popularItems", consumptionsModelHelper.popularItemsModel(targetsByConsumes, itemMap));
        
        return "brands/brand";
    }
    
    
}
