package com.metabroadcast.consumption;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.metabroadcast.purple.core.model.TargetRef;
import com.metabroadcast.purple.core.model.UserRef;

public class Consumption implements Comparable<Consumption> {

    private final TargetRef targetRef;
    private final UserRef userRef;
    private final DateTime timestamp;
    private final String channel;
    private final String publisher;
    private final String brandUri;

    public Consumption(UserRef userRef, TargetRef targetRef, DateTime timestamp, String channel, String publisher, String brandUri) {
        this.channel = channel;
        this.publisher = publisher;
        this.brandUri = brandUri;
        
        Preconditions.checkNotNull(userRef);
        Preconditions.checkNotNull(targetRef);
        Preconditions.checkNotNull(timestamp);
        
        this.userRef = userRef;
        this.targetRef = targetRef;
        this.timestamp = timestamp;
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
