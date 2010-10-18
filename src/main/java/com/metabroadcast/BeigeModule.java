package com.metabroadcast;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.webapp.properties.ContextConfigurer;
import com.metabroadcast.consumption.ConsumptionModule;
import com.metabroadcast.content.www.ContentModule;
import com.metabroadcast.invites.InvitesModule;
import com.metabroadcast.neighbours.NeighbourhoodModule;

@Configuration
@Import({CoreModule.class, SocialModule.class, WebModule.class, ConsumptionModule.class, InvitesModule.class, NeighbourhoodModule.class, PipeModule.class, ContentModule.class})
public class BeigeModule {
    
    public @Bean ContextConfigurer config() {
        ContextConfigurer c = new ContextConfigurer();
        c.init();
        return c;
    }
}
