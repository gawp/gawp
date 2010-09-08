package com.metabroadcast.neighbours;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.consumption.MongoConsumptionStore;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceOutput;

public class ScheduledNeighbourhoodUpdater {
    
    private static final String IDENTITY_MAP = "function () { emit(this.user, {refs: [this.target], count: 1}); };";
    private static final String COUNT_REDUCE = "function (key, values) {   var refs = [];   values.forEach( function (val) {     val.refs.forEach( function (ref) {       refs.push(ref);     });   });      return {refs: refs, count: refs.length}; };  ";
    private static final String SWAP_MAP = "function () {    var count = this.value.count;   var user = this._id;        this.value.refs.forEach( function (ref) {       emit(ref, {values: [{user: user, count: count}]});  }); };  ";
    private static final String ID_REDUCE = "function (key, values) {   var refs = [];  values.forEach( function (val) {        val.values.forEach( function (v) {          refs.push(v);       });     });     return {values: refs}; }  ";
    private static final String COMBINE_MAP = "function () {     var values = this.value.values;  values.forEach( function (val) {        values.forEach( function (inner) {                  if (val.user && inner.user && val.count && inner.count) {               var count = val.count+inner.count;                          if (val.user.userId > inner.user.userId) {                  emit({user1: val.user, user2: inner.user}, {counts: [count]});              } else if (val.user.userId < inner.user.userId) {                   emit({user1: inner.user, user2: val.user}, {counts: [count]});              }           }               });     }); }  ";
    private static final String SIMILARITY_REDUCE = "function (key, values) {     var counts = [];     var sum = 0;          values.forEach( function (val) {         val.counts.forEach( function (count) {             sum += count;             counts.push(count);         });     });          var length = counts.length;     var similarity = sum > 0 ? length / (sum - length) : 0;     return {counts: counts, similarity: similarity}; }  ";
    private static final String FINALIZE = "function (key, value) {     return value.similarity; }  ";
    private static final String USER_MAP = "function () {     if (this.value > 0.0) {         var user1 = this._id.user1.userId+this._id.user1.userNamespace+this._id.user1.appId;         var user2 = this._id.user2.userId+this._id.user2.userNamespace+this._id.user2.appId;              emit(user1, {neighbours: [{neighbour: this._id.user2, similarity: this.value}]});         emit(user2, {neighbours: [{neighbour: this._id.user1, similarity: this.value}]});     } }  ";
    private static final String USER_REDUCE = "function (key, values) {     var users = [];     values.forEach( function (val) {         val.neighbours.forEach( function (user) {             users.push(user);         });     });     users.sort(function (a, b) { return b.similarity - a.similarity; });     return {neighbours: users}; }  ";
    
    private final ScheduledExecutorService executorService;
    private final DBCollection db;
    
    public ScheduledNeighbourhoodUpdater(DatabasedMongo db) {
        this(db, Executors.newSingleThreadScheduledExecutor());
    }

    public ScheduledNeighbourhoodUpdater(DatabasedMongo db, ScheduledExecutorService executorService) {
        this.db = db.collection(MongoConsumptionStore.TABLE_NAME);
        this.executorService = executorService;
    }
    
    public void start() {
        executorService.scheduleWithFixedDelay(new NeighbourhoodJob(), 1, 2, TimeUnit.HOURS);
    }
    
    class NeighbourhoodJob implements Runnable {

        @Override
        public void run() {
            MapReduceOutput output = db.mapReduce(IDENTITY_MAP, COUNT_REDUCE, null, null);
            
            for (DBObject object: output.getOutputCollection().find()) {
                System.err.println(object);
            }
            
            output = output.getOutputCollection().mapReduce(SWAP_MAP, ID_REDUCE, null, null);
            
            for (DBObject object: output.getOutputCollection().find()) {
                System.err.println(object);
            }
            
            BasicDBObjectBuilder b = BasicDBObjectBuilder.start()
                .add( "mapreduce" , output.getOutputCollection().getName() )
                .add( "map" , COMBINE_MAP )
                .add( "reduce" , SIMILARITY_REDUCE )
                .add( "finalize", FINALIZE);
            output = output.getOutputCollection().mapReduce(b.get());
            
            for (DBObject object: output.getOutputCollection().find()) {
                System.err.println(object);
            }
            
            output = output.getOutputCollection().mapReduce(USER_MAP, USER_REDUCE, MongoNeighboursStore.TABLE_NAME, null);
            
            for (DBObject object: output.getOutputCollection().find()) {
                System.err.println(object);
            }
        }
    }
    
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}
