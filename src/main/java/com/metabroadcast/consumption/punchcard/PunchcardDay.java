package com.metabroadcast.consumption.punchcard;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PunchcardDay {

    private final Map<Integer, AtomicInteger> hourlyScore = Maps.newHashMapWithExpectedSize(24);
    
    public PunchcardDay() {
        for (int i=1; i<25; i++) {
            hourlyScore.put(i, new AtomicInteger(0));
        }
    }
    
    public void incrementHour(int hour) {
        Preconditions.checkArgument(hourlyScore.containsKey(hour));
        
        hourlyScore.get(hour).incrementAndGet();
    }
    
    public List<Integer> streamOfScores() {
        List<Integer> scores = Lists.newArrayList();
        
        for (AtomicInteger score: hourlyScore.values()) {
            scores.add(score.intValue());
        }
        
        return scores;
    }
    
    @Override
    public String toString() {
        return hourlyScore.toString();
    }
    
    @Override
    public int hashCode() {
        return hourlyScore.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PunchcardDay) {
            PunchcardDay target = (PunchcardDay) obj;
            
            return hourlyScore.equals(target);
        }
        return false;
    }
}
