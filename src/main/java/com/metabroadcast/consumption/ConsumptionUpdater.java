package com.metabroadcast.consumption;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.persistence.mongo.MongoUpdateBuilder;
import com.metabroadcast.content.ContentStore;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Controller
public class ConsumptionUpdater {
    private final Log log = LogFactory.getLog(ConsumptionUpdater.class);
    private final DBCollection table;
    private final ConsumptionTranslator consumptionTranslator = new ConsumptionTranslator();
    private final ContentStore contentStore;
    
    public ConsumptionUpdater(DatabasedMongo mongo, ContentStore contentStore) {
        this.contentStore = contentStore;
        table = mongo.collection(MongoConsumptionStore.TABLE_NAME);
    }
    
    @RequestMapping("/system/consumptions/update")
    public String updateAllConsumptions(HttpServletResponse response) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new AllConsumptionsUpdateJob());
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MimeType.TEXT_PLAIN.toString());
        response.getOutputStream().print("Started consumption updates");
        
        return null;
    }
    
    private class AllConsumptionsUpdateJob implements Runnable {
        @Override
        public void run() {
            try {
                updateAllConsumptions();
                log.info("Completed updating all indexes");
                System.out.println("Completed updating all indexes");
            } catch (Exception e) {
                log.error("Exception while running EngagementRankUpdater on all indexes", e);
                e.printStackTrace();
            }
        }
    }
    
    private void updateAllConsumptions() {
        DBCursor results = table.find();
        
        for (DBObject dbObject : results) {
            Consumption consumption = consumptionTranslator.fromDBObject(dbObject);
            
            String itemUri = consumption.targetRef().ref();
            
            Maybe<Description> maybeItem = contentStore.resolve(itemUri);
            if (maybeItem.hasValue() && maybeItem.requireValue() instanceof Item) {
                Item item = (Item) maybeItem.requireValue();
                if (item.getBrandSummary() != null) {
                    if (item.getBrandSummary().getUri() != consumption.getBrandUri()) {
                        table.update(consumptionTranslator.toQuery(consumption).build(), new MongoUpdateBuilder().setField(ConsumptionTranslator.BRAND_KEY, item.getBrandSummary().getUri()).build());
                    }
                }
            }
        }   
    }
}
