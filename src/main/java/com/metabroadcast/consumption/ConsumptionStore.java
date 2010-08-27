package com.metabroadcast.consumption;

import java.util.List;

import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.stats.Count;

public interface ConsumptionStore {

    public List<Consumption> find(UserRef userRef, DateTime from);
    
    public ImmutableMap<String, Count<String>> topBrands(int limit);

    public void store(Consumption consumption);

    public List<Count<Publisher>> findPublisherCounts(UserRef userRef, DateTime from);
    public List<Count<String>> findBrandCounts(UserRef userRef, DateTime from);
    public List<Count<String>> findChannelCounts(UserRef userRef, DateTime from);
}