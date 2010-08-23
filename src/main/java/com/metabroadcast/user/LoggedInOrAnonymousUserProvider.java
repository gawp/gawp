package com.metabroadcast.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.metabroadcast.purple.common.social.anonymous.AnonymousUserProvider;
import com.metabroadcast.purple.common.social.auth.RequestScopedAuthenticationProvider;
import com.metabroadcast.purple.common.social.model.UserRef;

public class LoggedInOrAnonymousUserProvider extends HandlerInterceptorAdapter implements UserProvider {

	private HttpServletRequest request;
	
    private RequestScopedAuthenticationProvider authenticationProvider;
	private AnonymousUserProvider anonymousUserProvider;
	
	@Override
	public UserRef existingUser() {
		if (authenticationProvider != null) {
			return authenticationProvider.principal();
		} else {
			return anonymousUserProvider.existingPrincipal(request);
		}
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		this.request = request;
		authenticationProvider.init(request, response);
		
		return true;
	}
	
	@Autowired
	public void setLoggedInUserProvider(RequestScopedAuthenticationProvider loggedInUserProvider) {
        this.authenticationProvider = loggedInUserProvider;
    }

	@Autowired
    public void setAnonymousUserProvider(AnonymousUserProvider anonymousUserProvider) {
        this.anonymousUserProvider = anonymousUserProvider;
    }
}
