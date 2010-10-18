package com.metabroadcast.content.www;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.consumption.ConsumedContent;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.consumption.www.ConsumedContentModelBuilder;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.user.www.UserModelHelper;

@Controller
public class ChannelsController {
    
    private final static int MAX_RECENT_ITEMS = 10;
    private final static int MAX_TOP_BRANDS = 8;
    
    private final ConsumptionStore consumptionStore;
    private final ContentStore contentStore;
    private final ConsumptionsModelHelper consumptionModelHelper;
    private final ConsumedContentProvider consumedContentProvider;
    private final ModelListBuilder<ConsumedContent> consumedContentModelBuilder;
    private final UserModelHelper userModelHelper;

    public ChannelsController(ContentStore contentStore, ConsumptionStore consumptionStore, ConsumptionsModelHelper consumptionModelHelper, ConsumedContentProvider consumedContentProvider,
                              UserModelHelper userModelHelper) {
        this.contentStore = contentStore;
        this.consumptionStore = consumptionStore;
        this.consumptionModelHelper = consumptionModelHelper;
        this.consumedContentProvider = consumedContentProvider;
        this.userModelHelper = userModelHelper;
        this.consumedContentModelBuilder = DelegatingModelListBuilder.delegateTo(
                new ConsumedContentModelBuilder(new SimplePlaylistAttributesModelBuilder(), new SimpleItemAttributesModelBuilder(), userModelHelper));
    }
    
    @RequestMapping(value = { "/channels/{channelName}" }, method = { RequestMethod.GET })
    public String getChannelPage(Map<String, Object> model, @PathVariable String channelName) {
        Channel channel = Channel.valueOf(channelName);
        model.put("channel", channel.toModel());
        
        List<ConsumedContent> recentConsumptions = consumedContentProvider.findForChannel(channel, MAX_RECENT_ITEMS);
        model.put("recentConsumptions", consumedContentModelBuilder.build(recentConsumptions));
        
        List<Consumption> consumptions = consumptionStore.recentConsumesOfChannel(channel.getUri(), new DateTime(DateTimeZones.UTC).minusWeeks(4));
        
        List<Count<String>> brandCounts = consumedContentProvider.findBrandCounts(consumptions);
        if (brandCounts.size() > MAX_TOP_BRANDS) {
            brandCounts = brandCounts.subList(0, MAX_TOP_BRANDS);
        }
                
        model.put("popularBrands", consumptionModelHelper.popularBrandsModel(brandCounts));
        
        return "channels/channel";
    }
}
