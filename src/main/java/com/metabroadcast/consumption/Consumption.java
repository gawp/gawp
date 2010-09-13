package com.metabroadcast.consumption;

import java.util.Set;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.content.Channel;

public class Consumption implements Comparable<Consumption> {

    private final TargetRef targetRef;
    private final UserRef userRef;
    private final DateTime timestamp;
    private final String channel;
    private final String publisher;
    private final String brandUri;
    private final Set<String> genres;
    
    public Consumption(UserRef userRef, TargetRef targetRef, DateTime timestamp, Channel channel, String publisher, String brandUri, Set<String> genres) {
        this.channel = channel.getUri();
        this.publisher = publisher;
        this.brandUri = brandUri;
        
        Preconditions.checkNotNull(userRef);
        Preconditions.checkNotNull(targetRef);
        Preconditions.checkNotNull(timestamp);
        
        this.userRef = userRef;
        this.targetRef = targetRef;
        this.timestamp = timestamp;
        this.genres = genres != null ? genres : Sets.<String>newHashSet();
    }
    
    public UserRef userRef() {
        return userRef;
    }
    
    public TargetRef targetRef() {
        return targetRef;
    }
    
    public DateTime timestamp() {
        return timestamp;
    }
    
    public String getChannel() {
        return channel;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getBrandUri() {
        return brandUri;
    }

    public String toKey() {
        return userRef.toKey()+":"+timestamp.getMillis();
    }
    
    public Set<String> getGenres() {
        return genres;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Consumption) {
            Consumption target = (Consumption) obj;
            return (userRef.equals(target.userRef) && targetRef.equals(targetRef) && timestamp.equals(target.timestamp));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return timestamp.hashCode();
    }
    
    public static Function<Consumption, TargetRef> TO_TARGET  = new Function<Consumption, TargetRef>() {

        @Override
        public TargetRef apply(Consumption consumption) {
            return consumption.targetRef();
        }
        
    };
    
    public static Function<Consumption, String> TO_TARGET_URIS  = new Function<Consumption, String>() {

        @Override
        public String apply(Consumption consumption) {
            return consumption.targetRef().ref();
        }
        
    };
    
    public static final Function<Consumption, String> TO_KEY = new Function<Consumption, String>() {

        @Override
        public String apply(Consumption consumption) {
            return consumption.toKey();
        }
        
    };

    @Override
    public int compareTo(Consumption o) {
        if (o == null) {
            return 1;
        }
        return timestamp.compareTo(o.timestamp);
    }
}
