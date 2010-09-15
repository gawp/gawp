package com.metabroadcast;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.social.auth.AuthenticationInterceptor;
import com.metabroadcast.common.social.auth.CookieAuthenticator;
import com.metabroadcast.common.social.auth.CookieTranslator;
import com.metabroadcast.common.social.auth.credentials.CredentialsStore;
import com.metabroadcast.common.social.user.AccessTokenProcessor;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.common.social.user.TwitterAccessTokenChecker;
import com.metabroadcast.common.social.user.UserDetailsProvider;
import com.metabroadcast.common.social.user.UserRefHelper;
import com.metabroadcast.common.webapp.json.JsonView;
import com.metabroadcast.common.webapp.soy.SoyTemplateRenderer;
import com.metabroadcast.common.webapp.soy.SoyTemplateViewResolver;
import com.metabroadcast.includes.www.IncludesController;
import com.metabroadcast.invites.Whitelist;
import com.metabroadcast.invites.WhitelistInterceptor;
import com.metabroadcast.user.www.TwitterAuthController;

@Configuration
public class WebModule {
    
	private static final String COOKIE_NAME = "beige";
	
	private @Value("${login.view}") String loginView;
	private @Value("${host}") String host;
    private @Value("${twitter.clientId}") String twitterClientId;
    
	private @Autowired CookieTranslator cookieTranslator;
	private @Autowired CredentialsStore credentialsStore;
	private @Autowired Whitelist whitelist;
	private @Autowired UserDetailsProvider userDetailsProvider;
	
	@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)
    public @Bean ApplicationIdAwareUserRefBuilder userRefHelper() {
        return new UserRefHelper();
    }
	
    public @Bean IncludesController getIncludesController() {
        return new IncludesController();
    }
    
    public @Bean TwitterAuthController twitterAuthController() {
        return new TwitterAuthController(cookieTranslator, twitterAccessTokenChecker(), twitterClientId, host, COOKIE_NAME);
    }
    
    public @Bean AccessTokenProcessor twitterAccessTokenChecker() {
        return new AccessTokenProcessor(new TwitterAccessTokenChecker(userRefHelper()), credentialsStore);
    }

    public @Bean DefaultAnnotationHandlerMapping controllerMappings() {
        DefaultAnnotationHandlerMapping controllerClassNameHandlerMapping = new DefaultAnnotationHandlerMapping();
        Object[] interceptors = { authenticationInterceptor(), whitelistInterceptor() };
        controllerClassNameHandlerMapping.setInterceptors(interceptors);
        return controllerClassNameHandlerMapping;
    }

    private final static Set<String> exceptions = ImmutableSet.of("/login", "/includes/javascript", "/invites", "/goodbye", "/logout");
    
    @Bean WhitelistInterceptor whitelistInterceptor() {
		return new WhitelistInterceptor(whitelist, cookieAuthenticator(), userDetailsProvider, exceptions);
	}
	public @Bean AuthenticationInterceptor authenticationInterceptor() {
        Map<String, List<String>> methodToPath = Maps.newHashMap();
        
        methodToPath.put("GET", ImmutableList.<String>of());
        methodToPath.put("POST", ImmutableList.<String>of());
        
        
        AuthenticationInterceptor authenticationInterceptor = new AuthenticationInterceptor();
        authenticationInterceptor.setViewResolver(resolver());
        authenticationInterceptor.setLoginView(loginView);
        authenticationInterceptor.setAuthService(cookieAuthenticator());
        authenticationInterceptor.setAuthenticationRequiredByMethod(methodToPath);
        authenticationInterceptor.setExceptions(exceptions);
        return authenticationInterceptor;
    }
    
	@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)
    public @Bean CookieAuthenticator cookieAuthenticator() {
		return new CookieAuthenticator();
    }

    public @Bean
    ViewResolver resolver() {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();

        resolver.setMediaTypes(ImmutableMap.of("json", MimeType.APPLICATION_JSON.toString()));

        resolver.setFavorPathExtension(true);
        resolver.setIgnoreAcceptHeader(true);
        resolver.setDefaultContentType(MediaType.TEXT_HTML);
        resolver.setDefaultViews(ImmutableList.<View> of(new JsonView()));

        SoyTemplateViewResolver soyResolver = new SoyTemplateViewResolver(soyRenderer());
        soyResolver.setNamespace("beige.templates");
        resolver.setViewResolvers(ImmutableList.<ViewResolver> of(soyResolver));
        return resolver;
    }

    @Bean SoyTemplateRenderer soyRenderer() {
        SoyTemplateRenderer renderer = new SoyTemplateRenderer();
        renderer.setPrefix("/WEB-INF/templates/");
        renderer.setSuffix(".soy");
        return renderer;
    }
}
