package com.metabroadcast.neighbours;

import java.util.List;

import com.google.inject.internal.Lists;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.translator.UserRefTranslator;
import com.mongodb.DBObject;

public class NeighbourTranslator {
    
    private final UserRefTranslator userTranslator = new UserRefTranslator();

    public Neighbour fromDBObject(DBObject dbObject) {
        UserRef userRef = userTranslator.fromDBObject((DBObject) dbObject.get("neighbour"));
        Number similarity = (Number) dbObject.get("similarity");
        
        return new Neighbour(userRef, similarity);
    }
    
    @SuppressWarnings("unchecked")
    public List<Neighbour> fromDBObjects(Iterable<DBObject> objects) {
        List<Neighbour> neighbours = Lists.newArrayList();
        for (DBObject object: objects) {
            List<DBObject> users = (List<DBObject>) ((DBObject) object.get("value")).get("neighbours");
            for (DBObject user: users) {
                neighbours.add(fromDBObject(user));
            }
        }
        return neighbours;
    }
}
