package com.metabroadcast.consumption;

import org.atlasapi.client.CachingJaxbAtlasClient;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.beige.bookmarklet.BookmarkletController;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.common.social.user.UserDetailsProvider;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.common.social.user.details.UserDetailsCache;
import com.metabroadcast.common.social.user.details.twitter.TwitterMasterUserDetailsProvider;
import com.metabroadcast.consumption.punchcard.CachingPunchcardStore;
import com.metabroadcast.consumption.punchcard.ConsumptionPunchcardProvider;
import com.metabroadcast.consumption.punchcard.MongoConsumptionPunchcardStore;
import com.metabroadcast.consumption.www.ConsumptionController;
import com.metabroadcast.content.AtlasContentStore;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.neighbours.NeighboursProvider;
import com.metabroadcast.user.Users;
import com.metabroadcast.user.twitter.TwitterUserRefProvider;
import com.metabroadcast.user.www.UserModelHelper;

@Configuration
public class ConsumptionModule {
    private @Autowired DatabasedMongo db;
    private @Autowired UserProvider userProvider;
    private @Autowired ApplicationIdAwareUserRefBuilder userRefBuilder;
    private @Autowired NeighboursProvider neighboursProvider;
    
    private @Value("${twitter.consumerKey}") String consumerKey;
    private @Value("${twitter.consumerSecret}") String consumerSecret;
    private @Value("${twitter.accessToken}") String twitterAccessToken;
    private @Value("${twitter.tokenSecret}") String twitterTokenSecret;
    private @Value("${accounts.refreshPeriodInMinutes}") int refreshPeriod;
	private @Value("${host}") String host;
	private @Value("${atlas}") String atlas;
    
    public @Bean ConsumptionStore consumptionStore() {
        return new MongoConsumptionStore(db);
    }
    
    public @Bean ConsumptionController consumptionController() {
        return new ConsumptionController(consumptionStore(), contentStore(), userProvider, userDetailsProvider(), userRefProvider(), neighboursProvider, userHelper(), consumedContentProvider(), punchcardProvider());
    }
    
    public @Bean ConsumedContentProvider consumedContentProvider() {
        return new ConsumedContentProvider(consumptionStore(), contentStore());
    }
    
    public @Bean TwitterUserRefProvider userRefProvider() {
        return new TwitterUserRefProvider(userRefBuilder);
    }
    
    public @Bean UserModelHelper userHelper() {
        return new UserModelHelper(userDetailsProvider());
    }
    
    public @Bean ContentStore contentStore() {
        return new AtlasContentStore(new CachingJaxbAtlasClient(atlas));
    }
    
    public @Bean UserDetailsProvider userDetailsProvider() {
        return new UserDetailsCache(new TwitterMasterUserDetailsProvider(consumerKey, consumerSecret, twitterAccessToken, twitterTokenSecret), getCacheDuration());
    }
    
    private Duration getCacheDuration() {
        return Duration.standardMinutes(refreshPeriod);
    }
    
	public @Bean BookmarkletController bookmarkletController() {
		return new BookmarkletController(contentStore(), host);
	}
	
	public @Bean ConsumptionPunchcardProvider punchcardProvider() {
	    ConsumptionPunchcardProvider delegate = new MongoConsumptionPunchcardStore(db);
	    return new CachingPunchcardStore(delegate, (Users) consumptionStore());
	}
}
