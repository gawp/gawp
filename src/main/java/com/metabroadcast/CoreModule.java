package com.metabroadcast;

import java.net.UnknownHostException;

import org.atlasapi.client.CachingJaxbAtlasClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.anonymous.AnonymousUserProvider;
import com.metabroadcast.common.social.anonymous.CookieBasedAnonymousUserProvider;
import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.auth.credentials.MongoDBCredentialsStore;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.common.social.user.FixedAppIdUserRefBuilder;
import com.metabroadcast.common.social.user.LoggedInOrAnonymousUserProvider;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.content.AtlasContentStore;
import com.metabroadcast.content.ContentStore;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Configuration
public class CoreModule {
    
    private @Value("${atlas.uri}") String atlas;
    private @Value("${atlas.apikey}") String apiKey;
    
    public @Bean CookieTranslator cookieTranslator() {
        return new CookieTranslator("beige", "f8bc218051364f2194f612182fc327c9");
    }

    public @Bean CredentialsStore credentialsStore() throws UnknownHostException, MongoException {
        return new MongoDBCredentialsStore(db());
    }

    public @Bean ApplicationIdAwareUserRefBuilder userRefBuilder() {
        return new FixedAppIdUserRefBuilder("beige");
    }

    public @Bean DatabasedMongo db() throws UnknownHostException, MongoException {
        return new DatabasedMongo(mongo(), "beige");
    }

    @Bean Mongo mongo() throws UnknownHostException, MongoException {
        return new Mongo();
    }
    
    public @Bean ContentStore contentStore() {
        return new AtlasContentStore(new CachingJaxbAtlasClient(atlas).withApiKey(apiKey));
    }
    
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public @Bean AnonymousUserProvider anonymousUserProvider() {
        return new CookieBasedAnonymousUserProvider(cookieTranslator(), userRefBuilder());
    }

    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public @Bean UserProvider userProvider() {
        LoggedInOrAnonymousUserProvider userProvider = new LoggedInOrAnonymousUserProvider();
        userProvider.setAnonymousUserProvider(anonymousUserProvider());
        userProvider.setLoggedInUserProvider(null);
        return userProvider;
    }
}
