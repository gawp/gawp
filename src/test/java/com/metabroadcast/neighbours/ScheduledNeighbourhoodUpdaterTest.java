package com.metabroadcast.neighbours;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.DeterministicScheduler;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.consumption.MongoConsumptionStore;

@RunWith(JMock.class)
public class ScheduledNeighbourhoodUpdaterTest {
    private final DateTime now = new DateTime(DateTimeZones.UTC);
    private final Mockery mockery = new JUnit4Mockery();
    private final DeterministicScheduler scheduler = new DeterministicScheduler();
    
    private final UserRef ben = new UserRef("101", UserNamespace.TWITTER, "beige");
    private final UserRef dan = new UserRef("102", UserNamespace.TWITTER, "beige");
    private final UserRef john = new UserRef("103", UserNamespace.TWITTER, "beige");
    
    private final TargetRef show1 = new TargetRef("101", "domain");
    private final TargetRef show2 = new TargetRef("102", "domain");
    private final TargetRef show3 = new TargetRef("103", "domain");
    private final TargetRef show4 = new TargetRef("104", "domain");
    
    private final Consumption consumption1 = new Consumption(ben, show1, now.minusSeconds(1), null, null, null, null);
    private final Consumption consumption2 = new Consumption(ben, show2, now.minusSeconds(2), null, null, null, null);
    private final Consumption consumption3 = new Consumption(john, show1, now.minusSeconds(3), null, null, null, null);
    private final Consumption consumption4 = new Consumption(john, show2, now.minusSeconds(4), null, null, null, null);
    private final Consumption consumption5 = new Consumption(john, show3, now.minusSeconds(5), null, null, null, null);
    private final Consumption consumption6 = new Consumption(dan, show3, now.minusSeconds(6), null, null, null, null);
    
    private ConsumptionStore store;
    private ScheduledNeighbourhoodUpdater updater;
    private NeighboursProvider provider;
    
    @Before
    public void setUp() {
        DatabasedMongo db = new DatabasedMongo(MongoTestHelper.anEmptyMongo(), "testing");
        
        updater = new ScheduledNeighbourhoodUpdater(db, scheduler);
        updater.start();
        
        store = new MongoConsumptionStore(db);
        store.store(consumption1);
        store.store(consumption2);
        store.store(consumption3);
        store.store(consumption4);
        store.store(consumption5);
        store.store(consumption6);
        
        provider = new MongoNeighboursStore(db);
    }
    
    @Test
    public void shouldGetNeighbours() {
        scheduler.tick(1, TimeUnit.HOURS);
        
        List<Neighbour> neighbours = provider.neighbours(ben);
        assertEquals(1, neighbours.size());
        assertEquals(john, neighbours.get(0).neighbour());
    }
}
