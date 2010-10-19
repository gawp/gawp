package com.metabroadcast.user.www;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.social.model.TwitterUserDetails;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.user.UserDetailsProvider;

public class UserModelHelper {
    private final UserDetailsProvider userDetailsProvider;
    private final Log log = LogFactory.getLog(getClass());

    public UserModelHelper(UserDetailsProvider userDetailsProvider) {
        this.userDetailsProvider = userDetailsProvider;
    }
    
    public SimpleModel userDetailsModel(TwitterUserDetails userDetails) {
        SimpleModel model = new SimpleModel();
        if (userDetails != null) {
            String targetName = userDetails.getFullName() != null ? userDetails.getFullName() : userDetails.getScreenName();
            String possessivePostfix = targetName.endsWith("s") ? "'" : "'s";
            
            model.put("screenName", userDetails.getScreenName());
            model.put("fullName", userDetails.getFullName());
            model.put("possessivePostfix", possessivePostfix);
            model.put("followers", userDetails.getFollowerCount());
            model.put("profileImage", userDetails.getProfileImage());
            model.put("largerProfileImage", getLargerProfileImageUrl(userDetails.getProfileImage()));
            model.put("profileUrl", userDetails.getProfileUrl());
            model.put("bio", userDetails.getBio());
            model.put("location", userDetails.getLocation());
            model.put("id", userDetails.getUserRef().getUserId());
        }
        return model;
    }
    
    private String getLargerProfileImageUrl(String normalUrl) {
        String extension = normalUrl.substring(normalUrl.lastIndexOf("_normal.") + "_normal.".length());
        String filename = normalUrl.substring(0, normalUrl.lastIndexOf("_normal."));
        
        return filename + "_reasonably_small." + extension;
    }
    
    public Maybe<UserDetails> getUserDetails(UserRef userRef) {
        try {
            Map<UserRef, UserDetails> userDetailsMap = userDetailsProvider.detailsFor(userRef, Lists.newArrayList(userRef));
            if (userDetailsMap.containsKey(userRef)) {
                return Maybe.fromPossibleNullValue(userDetailsMap.get(userRef));
            }
        } catch (Exception e) {
            log.warn("unable to get user details", e);
        }
        return Maybe.nothing();
    }
}
