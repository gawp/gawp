package com.metabroadcast.consumption;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.simple.Description;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.content.ContentStore;

public class ConsumedContentProvider {

    private final ConsumptionStore consumptionStore;
    private final ContentStore contentStore;

    public ConsumedContentProvider(ConsumptionStore consumptionStore, ContentStore contentStore) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
    }

    public List<ConsumedContent> find(UserRef userRef, DateTime from) {
        Map<String, ConsumedContent> consumedContent = Maps.newHashMap();
        for (Consumption consumption: consumptionStore.find(userRef, from)) {
            if (consumedContent.containsKey(consumption.targetRef().toKey())) {
                ConsumedContent current = consumedContent.get(consumption.targetRef().toKey());
                Consumption latestConsumption = consumption.timestamp().isAfter(current.getConsumption().timestamp()) ? consumption : current.getConsumption();
                Count<Consumption> count = current.getCount().plus(1L);
                consumedContent.put(consumption.targetRef().toKey(), new ConsumedContent(latestConsumption, current.getContent(), count));
            } else {
                Maybe<Description> description = contentStore.resolve(consumption.targetRef().ref());
                if (description.hasValue()) {
                    Count<Consumption> count = Count.of(consumption, 1L);
                    consumedContent.put(consumption.targetRef().toKey(), new ConsumedContent(consumption, description.requireValue(), count));
                }
            }
        }
        
        List<ConsumedContent> results = Lists.newArrayList(consumedContent.values());
        Collections.sort(results);
        return results;
    }
}
