package com.metabroadcast.consumption;

import org.atlasapi.media.entity.simple.Description;

import com.metabroadcast.common.stats.Count;

public class ConsumedContent implements Comparable<ConsumedContent> {

    private final Consumption consumption;
    private final Description content;
    private final Count<Consumption> count;

    public ConsumedContent(Consumption consumption, Description content, Count<Consumption> count) {
        this.consumption = consumption;
        this.content = content;
        this.count = count;
    }
    
    public Consumption getConsumption() {
        return consumption;
    }
    
    public Description getContent() {
        return content;
    }
    
    public Count<Consumption> getCount() {
        return count;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConsumedContent) {
            ConsumedContent target = (ConsumedContent) obj;
            
            return consumption.equals(target.consumption) && content.equals(target.content) && count.equals(target.count);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return consumption.hashCode();
    }
    
    @Override
    public String toString() {
        return consumption.toString()+" "+count.getCount()+" times";
    }

    @Override
    public int compareTo(ConsumedContent o) {
        if (o == null) {
            return 1;
        }
        
        return o.consumption.timestamp().compareTo(consumption.timestamp());
    }
}
