package com.metabroadcast.consumption.punchcard;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeConstants;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.model.SelfModelling;
import com.metabroadcast.common.model.SimpleModel;

public class Punchcard implements SelfModelling {
    
    private static final String IMAGE_URL = "http://chart.apis.google.com/chart?" +
    		"chs=800x300&chds=-1,24,-1,7,0,10&chf=bg,s,ffffff&" +
    		"chd=t:0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23" +
    		"0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23," +
    		"0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23," +
    		"0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23," +
    		"0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23," +
    		"0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23," +
    		"0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23," +
    		"0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23|" +
    		"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0," +
    		"1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," +
    		"2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2," +
    		"3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
    		"4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4," +
    		"5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5," +
    		"6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6," +
    		"7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7," +
    		"8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8|" +
    		"%s&" +
    		"chxt=x,y&chm=o,71B7E6,1,2.0,50.0&chxl=0:||1am|2|3|4|5|6|7|8|9|10|11|12|1pm|2|3|4|5|6|7|8|9|10|11|12||1:||Mon|Tue|Wed|Thr|Fri|Sat|Sun|&cht=s";

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

    @Override
    public SimpleModel toSimpleModel() {
        SimpleModel model = new SimpleModel();
        
        List<Integer> scores = streamOfHourlyScores();
        model.put("image", String.format(IMAGE_URL, join(scores, ",")));
        
        return model;
    }
    
    public static String join(Collection<Integer> s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<Integer> iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
}
