package com.metabroadcast.consumption;

import java.util.List;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;

import twitter4j.Status;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.twitter.stream.TweetProcessor;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.content.ContentStore;

public class StatusToConsumptionAdapter implements TweetProcessor {

    private final ContentStore contentStore;
    private final ConsumptionStore consumptionStore;
    private final MessageToSafeHtmlConverter urlConverter = new MessageToSafeHtmlConverter();
    private final ApplicationIdAwareUserRefBuilder userRefBuilder;

    public StatusToConsumptionAdapter(ConsumptionStore consumptionStore, ContentStore contentStore, ApplicationIdAwareUserRefBuilder userRefBuilder) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
        this.userRefBuilder = userRefBuilder;
    }
    
    @Override
    public void process(Status status) {
        List<String> urls = urlConverter.extractUrls(status.getText());
        if (urls != null && ! urls.isEmpty()) {
            for (Description content: contentStore.resolveAll(urls).values()) {
                Maybe<Item> item = ToConsumption.fromDescription(content);
                
                if (item.hasValue()) {
                    UserRef userRef = userRefBuilder.from(String.valueOf(status.getUser().getId()), UserNamespace.TWITTER);
                    Maybe<Consumption> consumption = ToConsumption.fromItem(userRef, item.requireValue());
                    
                    if (consumption.hasValue()) {
                        consumptionStore.store(consumption.requireValue());
                    }
                }
            }
        }
    }
}
