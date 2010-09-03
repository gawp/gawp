package com.metabroadcast.consumption;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.stats.Count;

public interface ConsumptionStore {

    public List<Consumption> find(UserRef userRef, int limit);
    public List<Consumption> find(UserRef userRef, DateTime from);
    
    public Map<String, Count<String>> topBrands(int limit);

    public void store(Consumption consumption);
}