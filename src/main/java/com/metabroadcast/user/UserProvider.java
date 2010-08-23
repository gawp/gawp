package com.metabroadcast.user;

import com.metabroadcast.purple.common.social.model.UserRef;

/**
 * I represent request-scoped objects that
 * provide either logged in or anonymous users.
 * 
 * @author John Ayres (john@metabroadcast.com)
 *  
 */
public interface UserProvider {

	UserRef existingUser();
	
}
