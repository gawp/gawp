package com.metabroadcast.invites;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoInviteStore implements InviteRequestStore, Whitelist, InviteAdministrator {

	private DBCollection invites;
	private DBCollection whitelist;

	public MongoInviteStore(DatabasedMongo db) {
		invites = db.collection("inviteRequests");
		whitelist = db.collection("whitelist");
	}
	
	@Override
	public void recordInviteRequest(String screenName) {
		String trimmedScreenName = trimAndCheckScreenName(screenName);
		
		if (isWhitelisted(trimmedScreenName)) {
			return;
		}
		invites.insert(new BasicDBObject(ID, trimmedScreenName));
	}

	public boolean isWhitelisted(String trimmedScreenName) {
	    Preconditions.checkNotNull(trimmedScreenName);
		Iterable<DBObject> found = where().idEquals(trimmedScreenName.toLowerCase()).find(whitelist);
		return !Iterables.isEmpty(found);
	}

	private static String trimAndCheckScreenName(String screenName) {
		Preconditions.checkNotNull(screenName);
		String trimmedScreenName = screenName.trim().toLowerCase();
		Preconditions.checkArgument(!trimmedScreenName.isEmpty());
		return trimmedScreenName;
	}

	@Override
	public void accept(String screenName) {
		BasicDBObject findByScreenName = new BasicDBObject(ID, trimAndCheckScreenName(screenName));
		whitelist.insert(findByScreenName);
		invites.remove(findByScreenName);
	}

	@Override
	public List<String> outstandingRequests() {
		Iterable<String> requestNames = Iterables.transform(invites.find(), new Function<DBObject, String>(){

			@Override
			public String apply(DBObject arg0) {
				return (String) arg0.get(ID);
			}
		});
		return ImmutableList.copyOf(requestNames);
	}
}
