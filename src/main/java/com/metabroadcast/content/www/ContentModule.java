package com.metabroadcast.content.www;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.user.www.UserModelHelper;

@Configuration
public class ContentModule {
    private @Autowired ContentStore contentStore;
    private @Autowired ConsumptionStore consumptionStore;
    private @Autowired UserProvider userProvider;
    private @Autowired UserModelHelper userHelper;
    private @Autowired ConsumedContentProvider consumedContentProvider;
    
    public @Bean BrandsController brandsController() {
        return new BrandsController(contentStore, consumptionStore, userProvider, userHelper, consumedContentProvider, new SimplePlaylistAttributesModelBuilder(), consumptionsModelHelper());
    }
    
    public @Bean ItemsController itemsController() {
        return new ItemsController(contentStore, consumptionStore, userProvider, userHelper, consumedContentProvider, new SimpleItemAttributesModelBuilder(), consumptionsModelHelper());
    }
    
    public @Bean ChannelsController channelsController() {
        return new ChannelsController(contentStore, consumptionStore, consumptionsModelHelper(), consumedContentProvider);
    }
    
    public @Bean ConsumptionsModelHelper consumptionsModelHelper() {
        return new ConsumptionsModelHelper(new SimpleItemAttributesModelBuilder(), new SimplePlaylistAttributesModelBuilder(), userHelper);
    }
}
