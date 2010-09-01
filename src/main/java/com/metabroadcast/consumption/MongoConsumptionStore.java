package com.metabroadcast.consumption;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
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

    private static final String TABLE_NAME = "consumption";

    private final Log log = LogFactory.getLog(getClass());
    private final ConsumptionTranslator translator = new ConsumptionTranslator();
    private final UserRefTranslator userRefTranslator = new UserRefTranslator();
    private final MapMaker mapMaker = new MapMaker().expiration(5, TimeUnit.MINUTES);
    private Map<String, Count<String>> topBrands = mapMaker.makeMap();

    private static final String REDUCE = "function(key , values ){" + "sum = 0;" + "for(var i in values) {"
                    + "sum += values[i];" + "}" + "return sum;" + "};";

    private static final String MAP = "function() {" + "emit(this.brand, 1);" + "}";

    private DBCollection table;

    public MongoConsumptionStore(DatabasedMongo db) {
        table = db.collection(TABLE_NAME);
    }
    
    public List<Consumption> find(UserRef userRef, int limit) {
        MongoQueryBuilder query = userRefTranslator.toQuery(userRef);
        return translator.fromDBObjects(table.find(query.build()).sort(
                        new BasicDBObject(ConsumptionTranslator.TIMESTAMP_KEY, -1)).limit(limit));
    }

    private List<Consumption> findFrom(UserRef userRef, DateTime from) {
        MongoQueryBuilder query = userRefTranslator.toQuery(userRef);
        query.fieldAfterOrAt(ConsumptionTranslator.TIMESTAMP_KEY, from);
        return translator.fromDBObjects(table.find(query.build()).sort(
                        new BasicDBObject(ConsumptionTranslator.TIMESTAMP_KEY, -1)));
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

    public List<Count<TargetRef>> findTargetCounts(UserRef userRef, DateTime from) {
        List<Count<TargetRef>> targetCounts = Lists.newArrayList();
        for (Count<String> count : findCounts(userRef, from, TARGET_KEY)) {
            targetCounts.add(Count.of(TargetRef.fromKey(count.getTarget()), count.getCount()));
        }
        return targetCounts;
    }

    public List<Count<Publisher>> findPublisherCounts(UserRef userRef, DateTime from) {
        List<Count<Publisher>> publisherCounts = Lists.newArrayList();
        for (Count<String> count : findCounts(userRef, from, PUBLISHER_KEY)) {
            publisherCounts.add(Count.of(Publisher.fromKey(count.getTarget()).requireValue(), count.getCount()));
        }
        return publisherCounts;
    }

    public List<Count<String>> findChannelCounts(UserRef userRef, DateTime from) {
        return findCounts(userRef, from, CHANNEL_KEY);
    }

    public List<Count<String>> findBrandCounts(UserRef userRef, DateTime from) {
        return findCounts(userRef, from, BRAND_KEY);
    }

    @SuppressWarnings("unchecked")
    public List<Count<String>> findCounts(UserRef userRef, DateTime from, Function<Consumption, String> keyFunction) {
        List<Consumption> consumptions = findFrom(userRef, from);
        Map<String, Count<String>> counts = Maps.newHashMap();

        for (Consumption consumption : consumptions) {
            String key = keyFunction.apply(consumption);
            if (key != null) {
                if (!counts.containsKey(key)) {
                    counts.put(key, new Count(key, 1L));
                } else {
                    counts.put(key, counts.get(key).plus(1L));
                }
            }
        }

        List<Count<String>> results = Lists.newArrayList(counts.values());
        Collections.sort(results);
        Collections.reverse(results);
        return results;
    }

    public static Function<Consumption, String> BRAND_KEY = new Function<Consumption, String>() {
        @Override
        public String apply(Consumption consumption) {
            return consumption.getBrandUri();
        }
    };

    public static Function<Consumption, String> PUBLISHER_KEY = new Function<Consumption, String>() {
        @Override
        public String apply(Consumption consumption) {
            return consumption.getPublisher();
        }
    };

    public static Function<Consumption, String> CHANNEL_KEY = new Function<Consumption, String>() {
        @Override
        public String apply(Consumption consumption) {
            return consumption.getChannel();
        }
    };

    public static Function<Consumption, String> TARGET_KEY = new Function<Consumption, String>() {
        @Override
        public String apply(Consumption consumption) {
            return consumption.targetRef().domain() + ":" + consumption.targetRef().ref();
        }
    };

    public void store(Consumption consumption) {
        table.save(translator.toDBObject(consumption));
    }
}
