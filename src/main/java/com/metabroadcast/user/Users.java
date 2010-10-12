package com.metabroadcast.user;

import java.util.List;

import com.metabroadcast.common.caching.ComputedValueListener;
import com.metabroadcast.common.social.model.UserRef;

public interface Users {
    
    public List<UserRef> users();
    
    public void addListener(ComputedValueListener<List<UserRef>> listener);
    
}
