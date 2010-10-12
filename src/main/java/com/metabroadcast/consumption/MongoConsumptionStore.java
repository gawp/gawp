package com.metabroadcast.consumption;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Ordering;
import com.google.common.collect.ImmutableMap.Builder;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.metabroadcast.common.stats.Count;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceOutput;

public class MongoConsumptionStore implements ConsumptionStore {

    public static final String TABLE_NAME = "consumption";

    private final Log log = LogFactory.getLog(getClass());
    private final ConsumptionTranslator translator = new ConsumptionTranslator();
    private final UserRefTranslator userRefTranslator = new UserRefTranslator();
    private final MapMaker mapMaker = new MapMaker().expiration(5, TimeUnit.MINUTES);
    private Map<String, Count<String>> topBrands = mapMaker.makeMap();

    private static final String REDUCE = "function(key , values ){ sum = 0;" + "for(var i in values) { sum += values[i];" + "}" + "return sum;" + "};";
    private static final String MAP = "function() { emit(this.brand, 1); }";

    private DBCollection table;

    public MongoConsumptionStore(DatabasedMongo db) {
        table = db.collection(TABLE_NAME);
    }
    
    public List<Consumption> find(UserRef userRef, int limit) {
        MongoQueryBuilder query = userRef != null ? userRefTranslator.toQuery(userRef) : new MongoQueryBuilder();
        return translator.fromDBObjects(table.find(query.build()).sort(
                        new BasicDBObject(ConsumptionTranslator.TIMESTAMP_KEY, -1)).limit(limit));
    }

    public List<Consumption> find(UserRef userRef, DateTime from) {
        MongoQueryBuilder query = userRef != null ? userRefTranslator.toQuery(userRef) : new MongoQueryBuilder();
        query.fieldAfterOrAt(ConsumptionTranslator.TIMESTAMP_KEY, from);
        return translator.fromDBObjects(table.find(query.build()).sort(
                        new BasicDBObject(ConsumptionTranslator.TIMESTAMP_KEY, -1)));
    }
    
    public Consumption findLatest(UserRef userRef, TargetRef targetRef) {
        MongoQueryBuilder query = userRef != null ? userRefTranslator.toQuery(userRef) : new MongoQueryBuilder();
        query.fieldEquals("target.domain", targetRef.domain()).fieldEquals("target.ref", targetRef.ref());
        List<Consumption> consumptions = translator.fromDBObjects(table.find(query.build()).sort(new BasicDBObject(ConsumptionTranslator.TIMESTAMP_KEY, -1)).limit(1));
        if (consumptions.isEmpty()) {
            return null;
        }
        return consumptions.get(0);
    }

    public Map<String, Count<String>> topBrands(int limit) {
        if (topBrands.isEmpty()) {
            try {
                MapReduceOutput output = table.mapReduce(MAP, REDUCE, null, new BasicDBObject("brand",
                                new BasicDBObject("$exists", Boolean.TRUE)));
                DBCursor results = output.results().sort(new BasicDBObject("value", -1)).limit(limit);

                Builder<String, Count<String>> builder = ImmutableMap.builder();
                for (DBObject dbObject : results) {
                    String brand = (String) dbObject.get(MongoConstants.ID);
                    long count = ((Double) dbObject.get("value")).longValue();
                    builder.put(brand, Count.of(brand, Ordering.arbitrary(), count));

                }
                topBrands = builder.build();
            } catch (Exception e) {
                log.warn("Problem getting topBrands", e);
            }
        }

        return ImmutableMap.copyOf(topBrands);
    }

    public void store(Consumption consumption) {
        table.save(translator.toDBObject(consumption));
    }

    @Override
    public void remove(Consumption consumption) {
        table.remove(translator.toDBObject(consumption));
    }

    @Override
    public List<Consumption> recentConsumesOfBrand(String brand) {
        MongoQueryBuilder query = new MongoQueryBuilder().fieldEquals(ConsumptionTranslator.BRAND_KEY, brand);
        return translator.fromDBObjects(table.find(query.build()).sort(new BasicDBObject(ConsumptionTranslator.TIMESTAMP_KEY, -1)));
    }
}
