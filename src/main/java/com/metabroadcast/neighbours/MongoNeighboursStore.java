package com.metabroadcast.neighbours;

import java.util.List;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.mongodb.DBCollection;

public class MongoNeighboursStore implements NeighboursProvider {
    public static final String TABLE_NAME = "neighbours";
    
    private final NeighbourTranslator translator = new NeighbourTranslator();
    private final DBCollection db;
    
    public MongoNeighboursStore(DatabasedMongo db) {
        this.db = db.collection(TABLE_NAME);
    }
    
    public List<Neighbour> neighbours(UserRef userRef, int limit) {
        MongoQueryBuilder query = new MongoQueryBuilder().fieldEquals("_id", userRef.toKey());
        return translator.fromDBObjects(db.find(query.build()));
    }

    public List<Neighbour> neighbours(UserRef userRef, UserNamespace namespace, int limit) {
        MongoQueryBuilder query = new MongoQueryBuilder().fieldEquals("_id", userRef.toKey()).fieldEquals("value.neighbours.neighbour.userNamespace", namespace.prefix());
        return translator.fromDBObjects(db.find(query.build()));
    }
}
