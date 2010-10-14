package com.metabroadcast.consumption;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.stats.Count;

public interface ConsumptionStore {

    public List<Consumption> find(UserRef userRef, int limit);
    public List<Consumption> find(UserRef userRef, DateTime from);
    public Consumption findLatest(UserRef userRef, TargetRef targetRef);
    
    public Map<String, Count<String>> topBrands(int limit);
    
    public List<Consumption> recentConsumesOfBrand(String brandUri);
    
    public List<Consumption> recentConsumesOfItem(String itemUri);
    
    public List<Consumption> recentConsumesOfChannel(String channelUri);
    
    public List<Consumption> recentConsumesOfGenre(String genreUri);

    public void store(Consumption consumption);
    
    public void remove(UserRef userRef, TargetRef targetRef);
}
