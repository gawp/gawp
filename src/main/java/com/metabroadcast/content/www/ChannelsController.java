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
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.ContentStore;

@Controller
public class ChannelsController {
    private final ConsumptionStore consumptionStore;
    private final ContentStore contentStore;
    private final ConsumptionsModelHelper consumptionModelHelper;
    private final ConsumedContentProvider consumedContentProvider;

    public ChannelsController(ContentStore contentStore, ConsumptionStore consumptionStore, ConsumptionsModelHelper consumptionModelHelper, ConsumedContentProvider consumedContentProvider) {
        this.contentStore = contentStore;
        this.consumptionStore = consumptionStore;
        this.consumptionModelHelper = consumptionModelHelper;
        this.consumedContentProvider = consumedContentProvider;
    }
    
    @RequestMapping(value = { "/channels/{channelName}" }, method = { RequestMethod.GET })
    public String getChannelPage(Map<String, Object> model, @PathVariable String channelName) {
        Channel channel = Channel.valueOf(channelName);
        model.put("channel", channel.toModel());
        
        List<Consumption> recentConsumptions = consumptionStore.recentConsumesOfChannel(channel.getUri());
        
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
        
        return "channels/channel";
    }
}
