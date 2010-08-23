package com.metabroadcast.user.www;

import static com.metabroadcast.common.url.UrlEncoding.encode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.credentials.AuthToken;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.user.AccessTokenProcessor;
import com.metabroadcast.common.social.user.AuthControllerHelper;

@Controller
public class TwitterAuthController {

	private static final String AUTH_URL_TEMPLATE = "https://oauth.twitter.com/2/authorize?oauth_callback_url=%s&oauth_mode=flow_web_app&oauth_client_identifier=%s";

	private static final String CALLBACK_URL = "/oauth-callback/twitter";

	private AuthControllerHelper authControllerHelper;

	private final String host;
	private final String clientId;

	private final AccessTokenProcessor accessTokenProcessor;
	private final CookieTranslator cookieTranslator;

	public TwitterAuthController(CookieTranslator cookieTranslator, AccessTokenProcessor accessTokenProcessor, @Value("${twitter.clientId}") String clientId, @Value("${host}") String host) {
		this.cookieTranslator = cookieTranslator;
		this.accessTokenProcessor = accessTokenProcessor;
		this.clientId = clientId;
		this.host = host;
		authControllerHelper = new AuthControllerHelper(cookieTranslator, accessTokenProcessor);
	}
	
    @RequestMapping(value = "/login/twitter", method = RequestMethod.GET)
    public View sendToTwitter(@RequestParam(required=true) String continueTo, HttpServletRequest request) {
    	return authControllerHelper.redirectToThirdPartyLogin(continueTo, request, authUrl(continueTo));
    }
    
    @RequestMapping(value = CALLBACK_URL)
    public Object springboard(HttpServletResponse response, @RequestParam(required=false) Boolean extractedFragment, @RequestParam(required=false) String continueTo, @RequestParam(value="oauth_access_token", required=false) String accessToken)  {
    	if (Boolean.TRUE.equals(extractedFragment)) {
    		Maybe<UserRef> user = accessTokenProcessor.process(new AuthToken(accessToken, UserNamespace.TWITTER));
			if (user.hasValue()) {
			    cookieTranslator.toResponse(response, user.requireValue());
			}
    		return new RedirectView(continueTo);
    	}
    	return "twitter/springboard";
    }
    
	private String authUrl(String continueTo) {
		return String.format(AUTH_URL_TEMPLATE, redirectUrl(continueTo), clientId);
	}

	private String redirectUrl(String continueTo) {
		return encode(host + CALLBACK_URL + "?continueTo=" + encode(continueTo));
	}
}
