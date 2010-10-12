package com.metabroadcast.user.twitter;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.twitter.TwitterJsonClient;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;

public class TwitterUserRefProvider {

	private static final String DETAILS_ENDPOINT_TEMPLATE = "http://api.twitter.com/1/users/show.json?screen_name=%s";
	private final TwitterJsonClient client;
    private ApplicationIdAwareUserRefBuilder userRefBuilder;
    private Map<String, UserRef> cachedUserRefs = new MapMaker().softValues().makeComputingMap(new Function<String, UserRef>() {
        @Override
        public UserRef apply(String screenname) {
            Map<String, Object> json = client.get(String.format(DETAILS_ENDPOINT_TEMPLATE, screenname));
            if (json == null || ! json.containsKey("id")) {
                return null;
            }
            
            String id = String.valueOf((Integer) json.get("id"));
            return userRefBuilder.from(id, UserNamespace.TWITTER);
        }
    });
	
	public TwitterUserRefProvider(TwitterJsonClient client, ApplicationIdAwareUserRefBuilder userRefBuilder) {
		this.client = client;
        this.userRefBuilder = userRefBuilder;
	}
	
	public TwitterUserRefProvider(ApplicationIdAwareUserRefBuilder userRefBuilder) {
		this(new TwitterJsonClient(), userRefBuilder);
	}
	
	public Maybe<UserRef> ref(String screenname) {
	    return Maybe.fromPossibleNullValue(cachedUserRefs.get(screenname));
	}
}
