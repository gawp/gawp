package com.metabroadcast.consumption;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.Description;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.model.TargetRef;
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

    public List<ConsumedContent> find(UserRef userRef, int limit) {
        Map<String, ConsumedContent> consumedContent = Maps.newHashMap();
        for (Consumption consumption: consumptionStore.find(userRef, limit)) {
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
    
    public List<Count<TargetRef>> findTargetCounts(List<Consumption> consumptions) {
        List<Count<TargetRef>> targetCounts = Lists.newArrayList();
        for (Count<String> count : findCounts(consumptions, TARGET_KEY)) {
            targetCounts.add(Count.of(TargetRef.fromKey(count.getTarget()), count.getCount()));
        }
        return targetCounts;
    }

    public List<Count<Publisher>> findPublisherCounts(List<Consumption> consumptions) {
        List<Count<Publisher>> publisherCounts = Lists.newArrayList();
        for (Count<String> count : findCounts(consumptions, PUBLISHER_KEY)) {
            publisherCounts.add(Count.of(Publisher.fromKey(count.getTarget()).requireValue(), count.getCount()));
        }
        return publisherCounts;
    }

    public List<Count<String>> findChannelCounts(List<Consumption> consumptions) {
        return findCounts(consumptions, CHANNEL_KEY);
    }

    public List<Count<String>> findBrandCounts(List<Consumption> consumptions) {
        return findCounts(consumptions, BRAND_KEY);
    }
    
    public List<Count<String>> findGenreCounts(List<Consumption> consumptions) {
        return findCounts(consumptions, GENRE_KEY);
    }

    @SuppressWarnings("unchecked")
    protected List<Count<String>> findCounts(List<Consumption> consumptions, Function<Consumption, Set<String>> keyFunction) {
        Map<String, Count<String>> counts = Maps.newHashMap();

        for (Consumption consumption : consumptions) {
            for (String key: keyFunction.apply(consumption)) {
                if (key != null) {
                    if (!counts.containsKey(key)) {
                        counts.put(key, new Count(key, 1L));
                    } else {
                        counts.put(key, counts.get(key).plus(1L));
                    }
                }
            }
        }

        List<Count<String>> results = Lists.newArrayList(counts.values());
        Collections.sort(results);
        Collections.reverse(results);
        return results;
    }

    protected static Function<Consumption, Set<String>> BRAND_KEY = new Function<Consumption, Set<String>>() {
        @Override
        public Set<String> apply(Consumption consumption) {
            return Sets.newHashSet(consumption.getBrandUri());
        }
    };

    protected static Function<Consumption, Set<String>> PUBLISHER_KEY = new Function<Consumption, Set<String>>() {
        @Override
        public Set<String> apply(Consumption consumption) {
            return Sets.newHashSet(consumption.getPublisher());
        }
    };

    protected static Function<Consumption, Set<String>> CHANNEL_KEY = new Function<Consumption, Set<String>>() {
        @Override
        public Set<String> apply(Consumption consumption) {
            return Sets.newHashSet(consumption.getChannel());
        }
    };
    
    protected static Function<Consumption, Set<String>> GENRE_KEY = new Function<Consumption, Set<String>>() {
        @Override
        public Set<String> apply(Consumption consumption) {
            return consumption.getGenres();
        }
    };

    protected static Function<Consumption, Set<String>> TARGET_KEY = new Function<Consumption, Set<String>>() {
        @Override
        public Set<String> apply(Consumption consumption) {
            return Sets.newHashSet(consumption.targetRef().domain() + ":" + consumption.targetRef().ref());
        }
    };
}
