package com.metabroadcast;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.anonymous.AnonymousUserProvider;
import com.metabroadcast.common.social.anonymous.CookieBasedAnonymousUserProvider;
import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.auth.credentials.MongoDBCredentialsStore;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.common.social.user.FixedAppIdUserRefBuilder;
import com.metabroadcast.common.social.user.LoggedInOrAnonymousUserProvider;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.common.webapp.properties.ContextConfigurer;
import com.metabroadcast.consumption.ConsumptionModule;
import com.metabroadcast.invites.InvitesModule;
import com.metabroadcast.neighbours.NeighbourhoodModule;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Configuration
@Import({WebModule.class, ConsumptionModule.class, InvitesModule.class, NeighbourhoodModule.class, PipeModule.class})
public class CoreModule {

    private @Autowired RequestScopedAuthenticationProvider authenticationProvider;

    public @Bean ContextConfigurer config() {
        ContextConfigurer c = new ContextConfigurer();
        c.init();
        return c;
    }
    
    public @Bean CookieTranslator cookieTranslator() {
        return new CookieTranslator("beige", "f8bc218051364f2194f612182fc327c9");
    }

    public @Bean CredentialsStore credentialsStore() throws UnknownHostException, MongoException {
        return new MongoDBCredentialsStore(mongo(), "credentials");
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

    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public @Bean AnonymousUserProvider anonymousUserProvider() {
        return new CookieBasedAnonymousUserProvider(cookieTranslator(), userRefBuilder());
    }

    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public @Bean UserProvider userProvider() {
        LoggedInOrAnonymousUserProvider userProvider = new LoggedInOrAnonymousUserProvider();
        userProvider.setAnonymousUserProvider(anonymousUserProvider());
        userProvider.setLoggedInUserProvider(authenticationProvider);
        return userProvider;
    }
}
