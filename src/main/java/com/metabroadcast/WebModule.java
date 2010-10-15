package com.metabroadcast;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.social.auth.AuthenticationInterceptor;
import com.metabroadcast.common.social.auth.AuthenticationInterceptor.UserNotAuthenticatedHandler;
import com.metabroadcast.common.social.auth.CookieAuthenticator;
import com.metabroadcast.common.social.user.UserDetailsProvider;
import com.metabroadcast.common.url.UrlEncoding;
import com.metabroadcast.common.webapp.json.JsonView;
import com.metabroadcast.common.webapp.soy.SoyTemplateRenderer;
import com.metabroadcast.common.webapp.soy.SoyTemplateViewResolver;
import com.metabroadcast.includes.www.IncludesController;
import com.metabroadcast.invites.Whitelist;
import com.metabroadcast.invites.WhitelistInterceptor;

@Configuration
public class WebModule {
    
	private @Value("${host}") String host;
	
	private @Autowired Whitelist whitelist;
	private @Autowired UserDetailsProvider userDetailsProvider;
	
    public @Bean IncludesController getIncludesController() {
        return new IncludesController();
    }
    
    public @Bean DefaultAnnotationHandlerMapping controllerMappings() {
        DefaultAnnotationHandlerMapping controllerClassNameHandlerMapping = new DefaultAnnotationHandlerMapping();
        Object[] interceptors = { authenticationInterceptor(), whitelistInterceptor() };
        controllerClassNameHandlerMapping.setInterceptors(interceptors);
        return controllerClassNameHandlerMapping;
    }

    private final static Set<String> exceptions = ImmutableSet.of("/login/twitter","/login", "/includes/javascript", "/invites", "/goodbye", "/logout", "/system");
    
    public @Bean WhitelistInterceptor whitelistInterceptor() {
		return new WhitelistInterceptor(whitelist, cookieAuthenticator(), userDetailsProvider, exceptions);
	}
    
	public @Bean AuthenticationInterceptor authenticationInterceptor() {
        Map<String, List<String>> methodToPath = Maps.newHashMap();
        
        methodToPath.put("GET", ImmutableList.<String>of("/bookmark/iframe"));
        methodToPath.put("POST", ImmutableList.<String>of());
        methodToPath.put("DELETE", ImmutableList.<String>of());
        
        
        AuthenticationInterceptor authenticationInterceptor = new AuthenticationInterceptor();
        authenticationInterceptor.setViewResolver(resolver());
        authenticationInterceptor.setUserNotAuthenticatedHandler(new UserNotAuthenticatedHandler() {
			
			@Override
			public boolean userNotAuthenticated(HttpServletRequest request, HttpServletResponse response) throws Exception {
				String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();
				response.sendRedirect(host + "/login?continueTo=" + UrlEncoding.encode(request.getRequestURI() + queryString));
				return false;
			}
		});
        
        authenticationInterceptor.setAuthService(cookieAuthenticator());
        authenticationInterceptor.setAuthenticationRequiredByMethod(methodToPath);
        authenticationInterceptor.setExceptions(Lists.newArrayList(exceptions));
        return authenticationInterceptor;
    }
    
	@Scope(value="request", proxyMode=ScopedProxyMode.TARGET_CLASS)
    public @Bean CookieAuthenticator cookieAuthenticator() {
		return new CookieAuthenticator();
    }

    public @Bean ViewResolver resolver() {
        ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();

        resolver.setMediaTypes(ImmutableMap.of("json", MimeType.APPLICATION_JSON.toString(), "js", "text/javascript"));

        resolver.setFavorPathExtension(true);
        resolver.setIgnoreAcceptHeader(true);
        resolver.setDefaultContentType(MediaType.TEXT_HTML);
        resolver.setDefaultViews(ImmutableList.<View> of(new JsonView()));

        SoyTemplateViewResolver htmlResolver = new SoyTemplateViewResolver(soyRenderer());
        htmlResolver.setNamespace("beige.templates");
        
        SoyTemplateViewResolver jsResolver = new SoyTemplateViewResolver(soyRenderer(), "text/javascript");
        jsResolver.setNamespace("beige.templates");
        
        resolver.setViewResolvers(Lists.<ViewResolver>newArrayList(htmlResolver, jsResolver));
        return resolver;
    }

    @Bean SoyTemplateRenderer soyRenderer() {
        SoyTemplateRenderer renderer = new SoyTemplateRenderer();
        renderer.setPrefix("/WEB-INF/templates/");
        renderer.setSuffix(".soy");
        return renderer;
    }
}
