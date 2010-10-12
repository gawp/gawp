package com.metabroadcast;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.caching.ComputedValueListener;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.twitter.stream.TweetProcessor;
import com.metabroadcast.common.social.twitter.stream.TwitterFilteredPipe;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.consumption.StatusToConsumptionAdapter;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.user.Users;

@Configuration
public class PipeModule {
    private @Value("${twitter.follow.username}") String followUsername;
    private @Value("${twitter.follow.password}") String followPassword;
    private @Autowired Users users;
    
    private @Autowired ConsumptionStore consumptionStore;
    private @Autowired ContentStore contentStore;
    private @Autowired ApplicationIdAwareUserRefBuilder userRefBuilder;
    
    @Bean TwitterFilteredPipe trackingTwitterPipe() {
        final TwitterFilteredPipe pipe = new TwitterFilteredPipe(statusAdaptor(), followUsername, followPassword);
        pipe.setUsersToFollow(users.users());
        
        users.addListener(new ComputedValueListener<List<UserRef>>() {
            @Override
            public void valueComputed(Maybe<List<UserRef>> oldUsers, Maybe<List<UserRef>> newUsers) {
                
                if (! oldUsers.equals(newUsers)) {
                    pipe.setUsersToFollow(users.users());
                    pipe.updateStream();
                }
            }
        });
        
        pipe.start();
        return pipe;
    }
    
    @Bean TweetProcessor statusAdaptor() {
        return new StatusToConsumptionAdapter(consumptionStore, contentStore, userRefBuilder);
    }
}
