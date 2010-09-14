package com.metabroadcast.invites;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.invites.www.InvitesController;

@Configuration
public class InvitesModule {

    public @Bean InvitesController invitesController() {
        return new InvitesController();
    }
}
