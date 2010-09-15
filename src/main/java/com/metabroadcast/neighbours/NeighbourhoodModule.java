package com.metabroadcast.neighbours;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
public class NeighbourhoodModule {
    private @Autowired DatabasedMongo db;

    public @Bean ScheduledNeighbourhoodUpdater neighbourhoodUpdater() {
        ScheduledNeighbourhoodUpdater updater = new ScheduledNeighbourhoodUpdater(db);
        updater.start();
        return updater;
    }
    
    public @Bean NeighboursProvider neighboursProvider() {
        return new MongoNeighboursStore(db);
    }
}
