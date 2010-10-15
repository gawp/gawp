package com.metabroadcast.consumption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.beige.bookmarklet.BookmarkletController;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.user.UserDetailsProvider;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.consumption.punchcard.CachingPunchcardStore;
import com.metabroadcast.consumption.punchcard.ConsumptionPunchcardProvider;
import com.metabroadcast.consumption.punchcard.MongoConsumptionPunchcardStore;
import com.metabroadcast.consumption.www.ConsumptionController;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.content.www.ConsumptionsModelHelper;
import com.metabroadcast.neighbours.NeighboursProvider;
import com.metabroadcast.user.Users;
import com.metabroadcast.user.twitter.TwitterUserRefProvider;
import com.metabroadcast.user.www.UserModelHelper;

@Configuration
public class ConsumptionModule {
    
    private @Autowired DatabasedMongo db;
    private @Autowired ContentStore contentStore;
    private @Autowired UserProvider userProvider;
    private @Autowired NeighboursProvider neighboursProvider;
    private @Autowired ConsumptionsModelHelper consumptionsModelHelper;
    private @Autowired UserDetailsProvider userDetailsProvider;
    private @Autowired TwitterUserRefProvider userRefProvider;
    private @Autowired UserModelHelper userModelHelper;
    
    private @Value("${host}") String host;
	
    
    public @Bean ConsumptionStore consumptionStore() {
        return new MongoConsumptionStore(db);
    }
    
    public @Bean ConsumptionController consumptionController() {
        return new ConsumptionController(consumptionStore(), contentStore, userProvider, userDetailsProvider, userRefProvider, neighboursProvider, userModelHelper, 
                                         consumedContentProvider(), punchcardProvider(), consumptionsModelHelper);
    }
    
    public @Bean ConsumedContentProvider consumedContentProvider() {
        return new ConsumedContentProvider(consumptionStore(), contentStore);
    }
    
    public @Bean ConsumptionUpdater consumptionUpdater() {
        return new ConsumptionUpdater(db, contentStore);
    }
    
    public @Bean ConsumptionsModelHelper consumptionsModelHelper() {
        return new ConsumptionsModelHelper(contentStore, new SimpleItemAttributesModelBuilder(), new SimplePlaylistAttributesModelBuilder(), userModelHelper);
    }
    
    public @Bean BookmarkletController bookmarkletController() {
		return new BookmarkletController(contentStore, host);
	}
	
	public @Bean ConsumptionPunchcardProvider punchcardProvider() {
	    ConsumptionPunchcardProvider delegate = new MongoConsumptionPunchcardStore(db);
	    return new CachingPunchcardStore(delegate, (Users) consumptionStore());
	}
}
