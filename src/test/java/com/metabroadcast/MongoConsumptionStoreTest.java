package com.metabroadcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.consumption.MongoConsumptionStore;


public class MongoConsumptionStoreTest {
    private UserRef alice = new UserRef("102", UserNamespace.FACEBOOK, null);
    private TargetRef target1 = new TargetRef("id1", "domain");
    private TargetRef target2 = new TargetRef("id2", "domain");
    private DateTime timestamp1 = new DateTime(DateTimeZones.UTC);
    private DateTime timestamp2 = new DateTime(DateTimeZones.UTC).minusDays(2);
    
    private Consumption consumption1 = new Consumption(alice, target1, timestamp1, null, null, null, null);
    private Consumption consumption2 = new Consumption(alice, target2, timestamp2, null, null, null, null);

    private ConsumptionStore store;
    
    @Before
    public void setUp() {
         store = new MongoConsumptionStore(new DatabasedMongo(MongoTestHelper.anEmptyMongo(), "testing"));
    }
    
    @Test
    public void shouldPersistAndRetrieve() {
        store.store(consumption1);
        store.store(consumption2);
        
        List<Consumption> consumptions = store.find(alice, 10);
        assertFalse(consumptions.isEmpty());
        assertEquals(Lists.newArrayList(consumption1, consumption2), consumptions);
        
        consumptions = store.find(alice, 1);
        assertFalse(consumptions.isEmpty());
        assertEquals(Lists.newArrayList(consumption1), consumptions);
    }
    
    @Test
    public void shouldRemove() {
        store.store(consumption1);
        
        List<Consumption> consumptions = store.find(alice, 10);
        assertFalse(consumptions.isEmpty());
        
        store.remove(consumption1.userRef(), consumption1.targetRef());
        
        consumptions = store.find(alice, 10);
        assertTrue(consumptions.isEmpty());
    }
}
