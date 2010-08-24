package com.metabroadcast;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.user.UserDetailsProvider;
import com.metabroadcast.common.social.user.details.twitter.TwitterMasterUserDetailsProvider;

public class UserDetailsProviderTest {
    private String consumerKey = "NirQJssqN1Nl0tLqstrm0w";
    private String consumerSecret = "YyOFuBzXPSQAyfDx3EYp4wsVX3iD9bzP8xp5WCW9fV4";
    private String twitterAccessToken = "163926561-RzQMcxfO50xTQ4pb5kyELjskLJT8pu3Mk20NYs";
    private String twitterTokenSecret = "IRCcgfJMaNUA7s7joUX1ulAXK5YqsYqcXiapOkCvBQ";
    
    private UserDetailsProvider provider = new TwitterMasterUserDetailsProvider(consumerKey, consumerSecret, twitterAccessToken, twitterTokenSecret);
    
    @Test
    public void shouldRetrieveDetails() {
        UserRef userRef = new UserRef(788695L, UserNamespace.TWITTER, "beige");
        
        Map<UserRef, UserDetails> userDetailsMap = provider.detailsFor(userRef, Lists.newArrayList(userRef));
        assertFalse(userDetailsMap.isEmpty());
        UserDetails userDetails = userDetailsMap.get(userRef);
        assertNotNull(userDetails);
    }
}
