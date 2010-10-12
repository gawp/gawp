package com.metabroadcast.consumption.punchcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.MongoConsumptionStore;

public class MongoConsumptionPunchcardProvider {

    private UserRef alice = new UserRef("102", UserNamespace.TWITTER, null);
    private TargetRef target1 = new TargetRef("id1", "domain");
    private TargetRef target2 = new TargetRef("id2", "domain");
    private DateTime timestamp1 = new DateTime(2010, 3, 1, 1, 0, 0, 0);
    private DateTime timestamp2 = new DateTime(2010, 3, 8, 1, 0, 0, 0);

    private Consumption consumption1 = new Consumption(alice, target1, timestamp1, null, null, null, null);
    private Consumption consumption2 = new Consumption(alice, target2, timestamp2, null, null, null, null);

    private MongoConsumptionStore store;
    private ConsumptionPunchcardProvider provider;

    @Before
    public void setUp() {
        DatabasedMongo db = new DatabasedMongo(MongoTestHelper.anEmptyMongo(), "testing");
        store = new MongoConsumptionStore(db);
        provider = new MongoConsumptionPunchcardStore(db);
    }

    @Test
    public void shouldRetrievePunchcard() throws Exception {
        store.store(consumption1);
        store.store(consumption2);

        Punchcard punchcard = provider.punchCard(alice);
        assertNotNull(punchcard);
        
        List<Integer> scores = punchcard.streamOfHourlyScores();
        assertEquals(24*7, scores.size());
        assertEquals(Integer.valueOf(2), scores.get(0));
        
        for (Integer score: scores.subList(1, scores.size())) {
            assertEquals(Integer.valueOf(0), score);
        }
        
        System.out.println(punchcard.toSimpleModel().asMap().get("image"));
    }
}
