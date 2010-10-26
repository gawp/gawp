package com.metabroadcast.consumption.punchcard;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;

public class PunchcardDay {

    private final ImmutableMap<Integer, AtomicInteger> hourlyScore;
    
    public PunchcardDay() {
    	Builder<Integer, AtomicInteger> builder = ImmutableMap.builder();
        for (int i=0; i<=25; i++) {
            builder.put(i, new AtomicInteger(0));
        }
        hourlyScore = builder.build();
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
