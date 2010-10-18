package com.metabroadcast.consumption;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.translator.TargetRefTranslator;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.metabroadcast.content.Channel;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ConsumptionTranslator {
    
    public static final String USER_REF_KEY = "user";
    public static final String TARGET_REF_KEY = "target";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String CHANNEL_KEY = "channel";
    public static final String PUBLISHER_KEY = "publisher";
    public static final String BRAND_KEY = "brand";
    public static final String GENRES_KEY = "genres";
    
    private final UserRefTranslator userTranslator = new UserRefTranslator();
    private final TargetRefTranslator targetTranslator = new TargetRefTranslator();
    
    public Consumption fromDBObject(DBObject dbObject) {
        UserRef userRef = userTranslator.fromDBObject((DBObject) dbObject.get(USER_REF_KEY));
        TargetRef targetRef = targetTranslator.fromDBObject((DBObject) dbObject.get(TARGET_REF_KEY));
        DateTime timestamp = TranslatorUtils.toDateTime(dbObject, TIMESTAMP_KEY);
        String channelUri = (String) dbObject.get(CHANNEL_KEY);
		Channel channel = channelUri == null ? null : Channel.fromUri(channelUri);
        String publisher = (String) dbObject.get(PUBLISHER_KEY);
        String brand = (String) dbObject.get(BRAND_KEY);
        Set<String> genres = TranslatorUtils.toSet(dbObject, GENRES_KEY);
        return new Consumption(userRef, targetRef, timestamp, channel, publisher, brand, genres);
    }
    
    public List<Consumption> fromDBObjects(Iterable<DBObject> objects) {
        List<Consumption> consumptions = Lists.newArrayList();
        for (DBObject dbObject: objects) {
            consumptions.add(fromDBObject(dbObject));
        }
        return consumptions;
    }

    public DBObject toDBObject(Consumption model) {
        DBObject dbObject = new BasicDBObject();
        TranslatorUtils.from(dbObject, "_id", model.toKey());
    
        TranslatorUtils.fromDateTime(dbObject, TIMESTAMP_KEY, model.timestamp());
        TranslatorUtils.from(dbObject, USER_REF_KEY, userTranslator.toDBObject(model.userRef()));
        TranslatorUtils.from(dbObject, TARGET_REF_KEY, targetTranslator.toDBObject(model.targetRef()));
        TranslatorUtils.from(dbObject, CHANNEL_KEY, model.getChannel());
        TranslatorUtils.from(dbObject, PUBLISHER_KEY, model.getPublisher());
        TranslatorUtils.from(dbObject, BRAND_KEY, model.getBrandUri());
        TranslatorUtils.fromSet(dbObject, model.getGenres(), GENRES_KEY);
        
        return dbObject;
    }
    
    public MongoQueryBuilder toQuery(Consumption consumption) {
        return new MongoQueryBuilder().idEquals(consumption.toKey());
    }
}
