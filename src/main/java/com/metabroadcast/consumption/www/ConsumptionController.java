package com.metabroadcast.consumption.www;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.DateTimeInQueryParser;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.consumption.ConsumedContent;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.ContentRefs;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.purple.common.social.model.TargetRef;
import com.metabroadcast.purple.common.social.model.UserRef;
import com.metabroadcast.user.UserProvider;

@Controller
public class ConsumptionController {
    
    private final ConsumptionStore consumptionStore;
    private final ConsumedContentProvider consumedContentProvider;
    private final DateTimeInQueryParser queryParser = new DateTimeInQueryParser();
    
    private final ModelListBuilder<ConsumedContent> consumedContentModelListBuilder = DelegatingModelListBuilder.delegateTo(new ConsumedContentModelBuilder(new SimplePlaylistAttributesModelBuilder(), new SimpleItemAttributesModelBuilder()));
    private final ContentStore contentStore;
    private final UserProvider userProvider;
    
    public ConsumptionController(ConsumptionStore consumptionStore, ContentStore contentStore, UserProvider userProvider) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
        this.consumedContentProvider = new ConsumedContentProvider(consumptionStore, contentStore);
        this.userProvider = userProvider;
    }
    
    @RequestMapping(value={"/watches"}, method={RequestMethod.GET})
    public String watches(@RequestParam(required=false) String from, Map<String, Object> model) {
        DateTime timestampFrom = from != null ? queryParser.parse(from) : new DateTime(DateTimeZones.UTC).minusDays(1);
        UserRef userRef = userProvider.existingUser();
        Preconditions.checkNotNull(timestampFrom);
        Preconditions.checkNotNull(userRef);
        
        List<ConsumedContent> consumedContent = consumedContentProvider.find(userRef, timestampFrom);
        model.put("items", consumedContentModelListBuilder.build(consumedContent));
        
        List<Count<String>> brands = consumptionStore.findBrandCounts(userRef, new DateTime(DateTimeZones.UTC).minusWeeks(1));
        addBrandCountsModel(model, brands);
        
        List<Count<String>> channels = consumptionStore.findChannelCounts(userRef, new DateTime(DateTimeZones.UTC).minusWeeks(1));
        addChannelCountsModel(model, channels);
        
        return "watches/list";
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
        
        model.put("channels", Channel.mapList());
        return "watches/watch";
    }
    
    @RequestMapping(value={"/watch"}, method={RequestMethod.GET})
    public String watchOptions(Map<String, Object> model) {
        
        model.put("channels", Channel.mapList());
        return "watches/watch";
    }
    
    private int max(List<Count<String>> counts) {
        int max = 0;
        for (Count<?> count: counts) {
            int current = Long.valueOf(count.getCount()).intValue();
            if (current > max) {
                max = current;
            }
        }
        
        return max;
    }
    
    private void addBrandCountsModel(Map<String, Object> model, List<Count<String>> brands) {
        List<Map<String, Object>> counts = Lists.newArrayList();
        int max = max(brands);
        model.put("max", max);
        
        for (Count<String> count: brands) {
            Map<String, Object> countMap = Maps.newHashMap();
            int countVal = Long.valueOf(count.getCount()).intValue();
            countMap.put("count", Long.valueOf(count.getCount()).intValue());
            countMap.put("width", Float.valueOf((float) countVal / (float) max * 100).intValue());
            
            Maybe<Description> desc = contentStore.resolve(count.getTarget());
            Map<String, Object> targetMap = Maps.newHashMap();
            if (desc.hasValue()) {
                Description brand = desc.requireValue();
                targetMap.put("title", brand.getTitle());
                targetMap.put("uri", brand.getUri());
                targetMap.put("logo", brand.getThumbnail());
                targetMap.put("link", brand.getUri());
            }
            
            countMap.put("target", targetMap);
            counts.add(countMap);
        }
        model.put("brands", counts);
    }
    
    private void addChannelCountsModel(Map<String, Object> model, List<Count<String>> channels) {
        List<Map<String, Object>> counts = Lists.newArrayList();
        int max = max(channels);
        model.put("max", max);
        
        for (Count<String> count: channels) {
            Map<String, Object> countMap = Maps.newHashMap();
            int countVal = Long.valueOf(count.getCount()).intValue();
            countMap.put("count", Long.valueOf(count.getCount()).intValue());
            countMap.put("width", Float.valueOf((float) countVal / (float) max * 100).intValue());
            
            Map<String, Object> targetMap = Maps.newHashMap();
            Channel channel = Channel.fromUri(count.getTarget());
            if (channel != null) {
                targetMap.put("title", channel.getName());
                targetMap.put("uri", channel.getUri());
                targetMap.put("logo", channel.getLogo());
                targetMap.put("link", channel.getUri());
            }
            
            countMap.put("target", targetMap);
            counts.add(countMap);
        }
        model.put("channels", counts);
    }
}
