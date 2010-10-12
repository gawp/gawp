package com.metabroadcast.consumption.punchcard;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeConstants;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Punchcard {

    Map<Integer, PunchcardDay> dailyScore = Maps.newHashMapWithExpectedSize(7);
    
    public Punchcard() {
        dailyScore.put(DateTimeConstants.MONDAY, new PunchcardDay());
        dailyScore.put(DateTimeConstants.TUESDAY, new PunchcardDay());
        dailyScore.put(DateTimeConstants.WEDNESDAY, new PunchcardDay());
        dailyScore.put(DateTimeConstants.THURSDAY, new PunchcardDay());
        dailyScore.put(DateTimeConstants.FRIDAY, new PunchcardDay());
        dailyScore.put(DateTimeConstants.SATURDAY, new PunchcardDay());
        dailyScore.put(DateTimeConstants.SUNDAY, new PunchcardDay());
    }
    
    public void incrementHourOfDay(int day, int hour) {
        Preconditions.checkArgument(dailyScore.containsKey(day));
        
        dailyScore.get(day).incrementHour(hour);
    }
    
    public List<Integer> streamOfHourlyScores() {
        List<Integer> scores = Lists.newArrayList();
        
        for (PunchcardDay day: dailyScore.values()) {
            scores.addAll(day.streamOfScores());
        }
        
        return scores;
    }
    
    @Override
    public String toString() {
        return dailyScore.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Punchcard) {
            Punchcard target = (Punchcard) obj;
            
            return dailyScore.equals(target.dailyScore);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return dailyScore.hashCode();
    }
}
