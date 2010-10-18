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
    private @Autowired UserModelHelper userModelHelper;
    private @Autowired ConsumedContentProvider consumedContentProvider;
    private @Autowired ConsumptionsModelHelper consumptionsModelHelper;
    
    public @Bean BrandsController brandsController() {
        return new BrandsController(contentStore, consumptionStore, userProvider, userModelHelper, consumedContentProvider, new SimplePlaylistAttributesModelBuilder(), consumptionsModelHelper);
    }
    
    public @Bean ItemsController itemsController() {
        return new ItemsController(contentStore, consumptionStore, userProvider, userModelHelper, consumedContentProvider, new SimpleItemAttributesModelBuilder(), consumptionsModelHelper);
    }
    
    public @Bean ChannelsController channelsController() {
        return new ChannelsController(contentStore, consumptionStore, consumptionsModelHelper, consumedContentProvider, userModelHelper);
    }
    
    public @Bean GenreController genresController() {
        return new GenreController(consumptionStore, contentStore, consumptionsModelHelper, consumedContentProvider);
    }
    
}
