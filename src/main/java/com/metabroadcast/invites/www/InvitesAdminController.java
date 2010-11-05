package com.metabroadcast.invites.www;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.auth.AuthenticationProvider;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.invites.InviteAdministrator;

@Controller
public class InvitesAdminController {

	private static final UserRef FRED = new UserRef(14426460, UserNamespace.TWITTER, "beige", null);
	private static final UserRef JOHN = new UserRef(821541, UserNamespace.TWITTER, "beige", null);
	private static final UserRef BEN = new UserRef(788695, UserNamespace.TWITTER, "beige", null);
	private final Set<UserRef> ADMINS = ImmutableSet.of(FRED, JOHN, BEN);
	
	private final InviteAdministrator inviteStore;
	private final AuthenticationProvider auth;

	public InvitesAdminController(InviteAdministrator inviteStore, AuthenticationProvider auth) {
		this.inviteStore = inviteStore;
		this.auth = auth;
	}
	
	@RequestMapping(value="/admin/invites", method=RequestMethod.GET)
	public String listInviteRequests(Map<String, Object> model) {
		if (!isAuthUser()){
			throw new RuntimeException("User is not an admin");
		}
		model.put("outstandingRequests", inviteStore.outstandingRequests());
		return "invites/list";
	}
	
	@RequestMapping(value="/admin/invites", method=RequestMethod.POST)
	public String acceptInviteRequest(@RequestParam("screename") String screenName, HttpServletResponse response){
		if (!isAuthUser()){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		inviteStore.accept(screenName);
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentLength(0);
		
		return null;
	}
	
	private boolean isAuthUser(){
		if (!auth.isAuthenticated()) {
			return false;
		}else{
			return ADMINS.contains(auth.principal());
		}
	}
}
