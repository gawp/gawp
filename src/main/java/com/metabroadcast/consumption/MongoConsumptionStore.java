package com.metabroadcast.consumption;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.purple.core.model.TargetRef;
import com.metabroadcast.purple.core.model.UserRef;
import com.metabroadcast.purple.core.model.translator.UserRefTranslator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class MongoConsumptionStore implements ConsumptionStore {

    private static final String TABLE_NAME = "consumption";

    private final Log log = LogFactory.getLog(getClass());
    private final ConsumptionTranslator translator = new ConsumptionTranslator();
    private final UserRefTranslator userRefTranslator = new UserRefTranslator();

    private DBCollection table;

    public MongoConsumptionStore(DatabasedMongo db) {
        table = db.collection(TABLE_NAME);
    }

    public List<Consumption> find(UserRef userRef, DateTime from) {
        MongoQueryBuilder query = userRefTranslator.toQuery(userRef);
        query.fieldAfterOrAt(ConsumptionTranslator.TIMESTAMP_KEY, from);
        return translator.fromDBObjects(table.find(query.build()).sort(new BasicDBObject(ConsumptionTranslator.TIMESTAMP_KEY, -1)));
    }

    @SuppressWarnings("unchecked")
    public List<Count<TargetRef>> findTargetCounts(UserRef userRef, DateTime from) {
        List<Consumption> consumptions = find(userRef, from);
        Map<TargetRef, Count<TargetRef>> counts = Maps.newHashMap();

        for (Consumption consumption : consumptions) {
            if (counts.containsKey(consumption.targetRef())) {
                counts.put(consumption.targetRef(), new Count(consumption.targetRef(), 1L));
            } else {
                counts.get(consumption.targetRef()).plus(1L);
            }
        }

        return Lists.newArrayList(counts.values());
    }

    @SuppressWarnings("unchecked")
    public List<Count<Publisher>> findPublisherCounts(UserRef userRef, DateTime from) {
        List<Consumption> consumptions = find(userRef, from);
        Map<Publisher, Count<Publisher>> counts = Maps.newHashMap();

        for (Consumption consumption : consumptions) {
            Maybe<Publisher> publisher = Publisher.fromKey(consumption.getPublisher());
            if (publisher.hasValue()) {
                if (!counts.containsKey(consumption.getPublisher())) {
                    counts.put(publisher.requireValue(), new Count(publisher.requireValue(), 1L));
                } else {
                    counts.get(publisher.requireValue()).plus(1L);
                }
            }
        }

        List<Count<Publisher>> results = Lists.newArrayList(counts.values());
        Collections.reverse(results);
        return results;
    }

    @SuppressWarnings("unchecked")
    public List<Count<String>> findChannelCounts(UserRef userRef, DateTime from) {
        List<Consumption> consumptions = find(userRef, from);
        Map<String, Count<String>> counts = Maps.newHashMap();

        for (Consumption consumption : consumptions) {
            if (consumption.getChannel() != null) {
                if (!counts.containsKey(consumption.getChannel())) {
                    counts.put(consumption.getChannel(), new Count(consumption.getChannel(), 1L));
                } else {
                    counts.put(consumption.getChannel(), counts.get(consumption.getChannel()).plus(1L));
                }
            }
        }

        List<Count<String>> results = Lists.newArrayList(counts.values());
        Collections.reverse(results);
        return results;
    }

    @SuppressWarnings("unchecked")
    public List<Count<String>> findBrandCounts(UserRef userRef, DateTime from) {
        List<Consumption> consumptions = find(userRef, from);
        Map<String, Count<String>> counts = Maps.newHashMap();

        for (Consumption consumption : consumptions) {
            if (consumption.getBrandUri() != null) {
                if (!counts.containsKey(consumption.getBrandUri())) {
                    counts.put(consumption.getBrandUri(), new Count(consumption.getBrandUri(), 1L));
                } else {
                    counts.put(consumption.getBrandUri(), counts.get(consumption.getBrandUri()).plus(1L));
                }
            }
        }

        List<Count<String>> results = Lists.newArrayList(counts.values());
        Collections.reverse(results);
        return results;
    }

    public void store(Consumption consumption) {
        table.save(translator.toDBObject(consumption));
    }
}
