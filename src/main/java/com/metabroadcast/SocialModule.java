package com.metabroadcast;

import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.user.AccessTokenProcessor;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.common.social.user.TwitterOAuth2AccessTokenChecker;
import com.metabroadcast.common.social.user.UserDetailsProvider;
import com.metabroadcast.common.social.user.details.UserDetailsCache;
import com.metabroadcast.common.social.user.details.twitter.TwitterMasterUserDetailsProvider;
import com.metabroadcast.user.twitter.TwitterUserRefProvider;
import com.metabroadcast.user.www.TwitterAuthController;
import com.metabroadcast.user.www.UserModelHelper;

@Configuration
public class SocialModule {
    
    private static final String COOKIE_NAME = "beige";
    
    private @Value("${twitter.clientId}") String twitterClientId;
    private @Value("${twitter.consumerKey}") String consumerKey;
    private @Value("${twitter.consumerSecret}") String consumerSecret;
    private @Value("${twitter.accessToken}") String twitterAccessToken;
    private @Value("${twitter.tokenSecret}") String twitterTokenSecret;
    
    private @Value("${accounts.refreshPeriodInMinutes}") int refreshPeriod;
    
    private @Value("${host}") String host;
    
    private @Autowired CookieTranslator cookieTranslator;
    private @Autowired CredentialsStore credentialsStore;
    private @Autowired ApplicationIdAwareUserRefBuilder userRefBuilder;
    
    public @Bean TwitterAuthController twitterAuthController() {
        return new TwitterAuthController(cookieTranslator, twitterAccessTokenChecker(), twitterClientId, host, COOKIE_NAME);
    }
    
    public @Bean AccessTokenProcessor twitterAccessTokenChecker() {
        return new AccessTokenProcessor(new TwitterOAuth2AccessTokenChecker(userRefBuilder), credentialsStore);
    }
    
    public @Bean TwitterUserRefProvider userRefProvider() {
        return new TwitterUserRefProvider(userRefBuilder);
    }
    
    public @Bean UserDetailsProvider userDetailsProvider() {
        return new UserDetailsCache(new TwitterMasterUserDetailsProvider(consumerKey, consumerSecret, twitterAccessToken, twitterTokenSecret), getCacheDuration());
    }
    
    public @Bean UserModelHelper userModelHelper() {
        return new UserModelHelper(userDetailsProvider());
    }
    
    private Duration getCacheDuration() {
        return Duration.standardMinutes(refreshPeriod);
    }
}
