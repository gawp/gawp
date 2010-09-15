package com.metabroadcast.invites;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.invites.www.InvitesController;

@Configuration
public class InvitesModule {

	private @Autowired DatabasedMongo db;
	
    public @Bean InvitesController invitesController() {
        return new InvitesController(inviteStore());
    }
    
    public @Bean MongoInviteStore inviteStore() {
    	return new MongoInviteStore(db);
    }
}
