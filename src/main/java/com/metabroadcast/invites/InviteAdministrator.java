package com.metabroadcast.invites;

import java.util.List;

public interface InviteAdministrator {

	void accept(String screenName);
	
	List<String> outstandingRequests();
	
}
