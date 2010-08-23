package com.metabroadcast;

import java.net.UnknownHostException;

import org.atlasapi.client.CachingJaxbAtlasClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.webapp.properties.ContextConfigurer;
import com.metabroadcast.consumption.ConsumptionModule;
import com.metabroadcast.content.AtlasContentStore;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.purple.common.social.anonymous.AnonymousUserProvider;
import com.metabroadcast.purple.common.social.anonymous.CookieBasedAnonymousUserProvider;
import com.metabroadcast.purple.common.social.auth.CookieTranslator;
import com.metabroadcast.purple.common.social.auth.RequestScopedAuthenticationProvider;
import com.metabroadcast.purple.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.purple.common.social.user.FixedAppIdUserRefBuilder;
import com.metabroadcast.user.LoggedInOrAnonymousUserProvider;
import com.metabroadcast.user.UserProvider;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Configuration
@Import({WebModule.class, ConsumptionModule.class})
@ImportResource("context.xml")
public class CoreModule {
    
    private @Autowired RequestScopedAuthenticationProvider authenticationProvider;
    
	public @Bean ContextConfigurer config() {
		ContextConfigurer c = new ContextConfigurer();
		c.init();
		return c;
	}
	
	public @Bean CookieTranslator cookieTranslator() {
        return new CookieTranslator("beige", "devsalt");
    }
	
	public @Bean ApplicationIdAwareUserRefBuilder userRefBuilder() {
		return new FixedAppIdUserRefBuilder("beige");
	}
	
	public @Bean DatabasedMongo db() throws UnknownHostException, MongoException {
		return new DatabasedMongo(new Mongo(), "beige");
	}
	
	@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)
	public @Bean AnonymousUserProvider anonymousUserProvider() {
	    return new CookieBasedAnonymousUserProvider(cookieTranslator(), userRefBuilder());
	}
	
	@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)
	public @Bean UserProvider userProvider() {
	    LoggedInOrAnonymousUserProvider userProvider = new LoggedInOrAnonymousUserProvider();
	    userProvider.setAnonymousUserProvider(anonymousUserProvider());
	    userProvider.setLoggedInUserProvider(authenticationProvider);
	    return userProvider;
	}
	
	public @Bean ContentStore contentStore() {
        return new AtlasContentStore(new CachingJaxbAtlasClient());
    }
}
