package com.metabroadcast.invites;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.social.auth.AuthenticationProvider;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.user.UserDetailsProvider;

public class WhitelistInterceptor extends HandlerInterceptorAdapter {

	private static final String INVITE_URL = "/invites";
	
	private final AuthenticationProvider auth;
	private final UserDetailsProvider userDetailsProvider;
	private final Whitelist whitelist;
	private final Set<String> exceptions;

	public WhitelistInterceptor(Whitelist whitelist, AuthenticationProvider auth, UserDetailsProvider userDetailsProvider, Set<String> exceptions) {
		this.whitelist = whitelist;
		this.auth = auth;
		this.userDetailsProvider = userDetailsProvider;
		this.exceptions = exceptions;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (auth.isAuthenticated() && !exceptions.contains(request.getRequestURI())) {
			UserRef principal = auth.principal();
			Map<UserRef, UserDetails> detailsMap = userDetailsProvider.detailsFor(principal, ImmutableList.of(principal));
			UserDetails details = Iterables.getOnlyElement(detailsMap.values());
			if (!whitelist.isWhitelisted(details.getScreenName())) {
				response.sendRedirect(INVITE_URL);
				return false;
			}
		}
		return super.preHandle(request, response, handler);
	}
}
