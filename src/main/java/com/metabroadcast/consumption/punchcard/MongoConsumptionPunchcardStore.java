package com.metabroadcast.consumption.punchcard;

import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.metabroadcast.consumption.ConsumptionTranslator;
import com.metabroadcast.consumption.MongoConsumptionStore;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoConsumptionPunchcardStore implements ConsumptionPunchcardProvider {
    
    private final DBCollection table;
    private final UserRefTranslator userRefTranslator = new UserRefTranslator();
    
    public MongoConsumptionPunchcardStore(DatabasedMongo db) {
        table = db.collection(MongoConsumptionStore.TABLE_NAME);
    }

    @Override
    public Punchcard punchCard(UserRef userRef) {
        Preconditions.checkNotNull(userRef);
        
        MongoQueryBuilder query = userRefTranslator.toQuery(userRef);
        DBCursor results = table.find(query.build());
        
        Punchcard punchcard = new Punchcard();
        for (DBObject dbObject : results) {
            DateTime timestamp = TranslatorUtils.toDateTime(dbObject, ConsumptionTranslator.TIMESTAMP_KEY);
            if (timestamp != null) {
                punchcard.incrementHourOfDay(timestamp.getDayOfWeek(), timestamp.getHourOfDay());
            }
        }
        
        return punchcard;
    }
}
