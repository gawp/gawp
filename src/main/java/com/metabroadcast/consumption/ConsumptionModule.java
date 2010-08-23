package com.metabroadcast.consumption;

import org.atlasapi.client.CachingJaxbAtlasClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.consumption.www.ConsumptionController;
import com.metabroadcast.content.AtlasContentStore;
import com.metabroadcast.content.ContentStore;

@Configuration
public class ConsumptionModule {
    private @Autowired DatabasedMongo db;
    private @Autowired UserProvider userProvider;
    
    public @Bean ConsumptionStore consumptionStore() {
        return new MongoConsumptionStore(db);
    }
    
    public @Bean ConsumptionController consumptionController() {
        return new ConsumptionController(consumptionStore(), contentStore(), userProvider);
    }
    
    public @Bean ContentStore contentStore() {
        return new AtlasContentStore(new CachingJaxbAtlasClient());
    }
}
