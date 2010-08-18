package com.metabroadcast.consumption.www;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.DateTimeInQueryParser;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.ContentRefs;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.purple.core.model.TargetRef;
import com.metabroadcast.purple.core.model.UserRef;
import com.metabroadcast.user.UserProvider;

@Controller
public class ConsumptionController {
    
    private final ConsumptionStore consumptionStore;
    private final DateTimeInQueryParser queryParser = new DateTimeInQueryParser();
    
    private final ModelListBuilder<Item> itemsModelListBuilder = DelegatingModelListBuilder.delegateTo(new SimpleItemAttributesModelBuilder());
    private final ModelListBuilder<Playlist> playlistsModelListBuilder = DelegatingModelListBuilder.delegateTo(new SimplePlaylistAttributesModelBuilder());
    private final ContentStore contentStore;
    private final UserProvider userProvider;
    
    private final Channel bbcone = new Channel("BBC One", "http://www.bbc.co.uk/services/bbcone/london", "");
    private final Channel bbctwo = new Channel("BBC Two", "http://www.bbc.co.uk/services/bbctwo/england", "");
    private final Channel bbcnews = new Channel("BBC News", "http://www.bbc.co.uk/services/bbcnews", "");
    private final Channel bbcparliment = new Channel("BBC Parliment", "http://www.bbc.co.uk/services/parliament", "");
    
    private final List<Channel> channels = ImmutableList.of(bbcone, bbctwo, bbcnews, bbcparliment);

    public ConsumptionController(ConsumptionStore consumptionStore, ContentStore contentStore, UserProvider userProvider) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
        this.userProvider = userProvider;
    }
    
    @RequestMapping(value={"/watches"}, method={RequestMethod.GET})
    public String watches(@RequestParam String from, Map<String, Object> model) {
        DateTime timestampFrom = queryParser.parse(from);
        UserRef userRef = userProvider.existingUser();
        Preconditions.checkNotNull(timestampFrom);
        Preconditions.checkNotNull(userRef);
        
        List<Consumption> consumptions = consumptionStore.find(userRef, timestampFrom);
        
        List<Item> items = contentStore.itemsByUri(Iterables.transform(consumptions, Consumption.TO_TARGET_URIS));
        
        model.put("items", itemsModelListBuilder.build(items));
        
        return "watches/list";
    }
    
    @RequestMapping(value={"/watches/brands"}, method={RequestMethod.GET})
    public String watchesBrands(@RequestParam String from, Map<String, Object> model) {
        DateTime timestampFrom = queryParser.parse(from);
        UserRef userRef = userProvider.existingUser();
        Preconditions.checkNotNull(timestampFrom);
        Preconditions.checkNotNull(userRef);
        
        List<Count<String>> brands = consumptionStore.findBrandCounts(userRef, timestampFrom);
        
        List<Map<String, Object>> counts = Lists.newArrayList();
        for (Count<String> count: brands) {
            Map<String, Object> countMap = Maps.newHashMap();
            countMap.put("count", Long.valueOf(count.getCount()).intValue());
            countMap.put("target", count.getTarget());
            counts.add(countMap);
        }
        model.put("counts", counts);
        
        return "watches/graph";
    }
    
    @RequestMapping(value={"/watches/channels"}, method={RequestMethod.GET})
    public String watchesChannels(@RequestParam String from, Map<String, Object> model) {
        DateTime timestampFrom = queryParser.parse(from);
        UserRef userRef = userProvider.existingUser();
        Preconditions.checkNotNull(timestampFrom);
        Preconditions.checkNotNull(userRef);
        
        List<Count<String>> brands = consumptionStore.findChannelCounts(userRef, timestampFrom);
        
        List<Map<String, Object>> counts = Lists.newArrayList();
        for (Count<String> count: brands) {
            Map<String, Object> countMap = Maps.newHashMap();
            countMap.put("count", Long.valueOf(count.getCount()).intValue());
            countMap.put("target", count.getTarget());
            counts.add(countMap);
        }
        model.put("counts", counts);
        
        return "watches/graph";
    }
    
    @RequestMapping(value={"/watch"}, method={RequestMethod.POST})
    public String watch(@RequestParam String channel, Map<String, Object> model) {
        UserRef userRef = userProvider.existingUser();
        Preconditions.checkNotNull(userRef);
        
        TargetRef targetRef = null; 
        List<Item> items = contentStore.getItemOnNow(channel);
        if (!items.isEmpty()) {
            Item item = items.get(0);
            BrandSummary brand = item.getBrandSummary();
            PublisherDetails publisher = item.getPublisher();
            targetRef = new TargetRef(item.getUri(), ContentRefs.ITEM_DOMAIN);
            consumptionStore.store(new Consumption(userRef, targetRef, new DateTime(DateTimeZones.UTC), channel, publisher != null ? publisher.getKey() : null, brand != null ? brand.getUri() : null));
            model.put("success", "Enjoy!");
        } else {
            model.put("error", "Unfortunately, there's nothing on that channel");
        }
        
        
        model.put("channels", channelsModel());
        return "watches/watch";
    }
    
    @RequestMapping(value={"/watch"}, method={RequestMethod.GET})
    public String watchOptions(Map<String, Object> model) {
        
        model.put("channels", channelsModel());
        return "watches/watch";
    }
    
    private List<Map<String, String>> channelsModel() {
        List<Map<String, String>> channelList = Lists.newArrayList();
        for (Channel channel: channels) {
            Map<String, String> channelMap = Maps.newHashMap();
            channelMap.put("name", channel.getName());
            channelMap.put("uri", channel.getUri());
            channelMap.put("logo", channel.getLogo());
            channelList.add(channelMap);
        }
        return channelList;
    }
}
