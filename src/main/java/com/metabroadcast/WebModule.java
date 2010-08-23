package com.metabroadcast;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.social.auth.AuthenticationInterceptor;
import com.metabroadcast.common.social.auth.RequestScopedAuthenticationProvider;
import com.metabroadcast.common.webapp.json.JsonView;
import com.metabroadcast.common.webapp.soy.SoyTemplateRenderer;
import com.metabroadcast.common.webapp.soy.SoyTemplateViewResolver;
import com.metabroadcast.includes.www.IncludesController;

@Configuration
public class WebModule {
    
    private @Autowired RequestScopedAuthenticationProvider authProvider;
	private @Value("${login.view}") String loginView;
		
    public @Bean IncludesController getIncludesController() {
        return new IncludesController();
    }

    public @Bean DefaultAnnotationHandlerMapping controllerMappings() {
        DefaultAnnotationHandlerMapping controllerClassNameHandlerMapping = new DefaultAnnotationHandlerMapping();
        Object[] interceptors = { getAuthenticationInterceptor() };
        controllerClassNameHandlerMapping.setInterceptors(interceptors);
        return controllerClassNameHandlerMapping;
    }

    public @Bean AuthenticationInterceptor getAuthenticationInterceptor() {
        Map<String, List<String>> methodToPath = Maps.newHashMap();
        
        methodToPath.put("GET", ImmutableList.<String>of());
        methodToPath.put("POST", ImmutableList.<String>of());
        
        List<String> exceptions = ImmutableList.of("/login", "/includes/javascript", "/stats/chart", "/goodbye", "/notTrackedUser");
        
        AuthenticationInterceptor authenticationInterceptor = new AuthenticationInterceptor();
        authenticationInterceptor.setViewResolver(resolver());
        authenticationInterceptor.setLoginView(loginView);
        authenticationInterceptor.setAuthService(authProvider);
        authenticationInterceptor.setAuthenticationRequiredByMethod(methodToPath);
        authenticationInterceptor.setExceptions(exceptions);
//        authenticationInterceptor.setUserNotAuthenticatedHandler(getUserNotAuthenticatedHandler());
        return authenticationInterceptor;
    }
    
//    public @Bean UserNotAuthenticatedHandler getUserNotAuthenticatedHandler() {
//        return new RedirectingUserNotAuthenticatedHandler(authProvider, resolver(), loginView, notTrackedUserView);
//    }

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

    @Bean
    SoyTemplateRenderer soyRenderer() {
        SoyTemplateRenderer renderer = new SoyTemplateRenderer();
        renderer.setPrefix("/WEB-INF/templates/");
        renderer.setSuffix(".soy");
        return renderer;
    }
}
