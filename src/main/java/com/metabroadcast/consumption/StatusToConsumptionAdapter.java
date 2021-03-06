package com.metabroadcast.consumption;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;

import twitter4j.Status;

import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.twitter.stream.TweetProcessor;
import com.metabroadcast.common.social.user.ApplicationIdAwareUserRefBuilder;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.user.Users;

public class StatusToConsumptionAdapter implements TweetProcessor {

    private final ContentStore contentStore;
    private final ConsumptionStore consumptionStore;
    private final MessageToSafeHtmlConverter urlConverter = new MessageToSafeHtmlConverter();
    private final ApplicationIdAwareUserRefBuilder userRefBuilder;
    private final static Map<String, Channel> CHANNEL_TAGS = ImmutableMap.<String, Channel> of("bbcone", Channel.BBC_ONE, "bbctwo", Channel.BBC_TWO, "bbcthree", Channel.BBC_THREE, "bbcfour",
            Channel.BBC_FOUR);
    private final static Pattern TAG = Pattern.compile("#(\\w+)");
    private final Users users;

    public StatusToConsumptionAdapter(ConsumptionStore consumptionStore, ContentStore contentStore, ApplicationIdAwareUserRefBuilder userRefBuilder, Users users) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
        this.userRefBuilder = userRefBuilder;
        this.users = users;
    }

    @Override
    public void process(Status status) {
        String text = status.getText();
        UserRef userRef = userRefBuilder.from(String.valueOf(status.getUser().getId()), UserNamespace.TWITTER);

        if (users.users().contains(userRef)) {
            List<String> urls = urlConverter.extractUrls(text);
            Item item = null;
            Channel channel = null;

            if (urls != null && !urls.isEmpty()) {
                for (Description content : contentStore.resolveAll(urls).values()) {
                    Maybe<Item> possibleItem = Converters.fromDescription(content);

                    if (possibleItem.hasValue()) {
                        item = possibleItem.requireValue();
                    }
                }
            } else {
                String textLc = text.toLowerCase();
                if (isRelevant(textLc)) {
                    Matcher matcher = TAG.matcher(textLc);
                    while (matcher.find()) {
                        String currentTag = matcher.group(1);
                        if (CHANNEL_TAGS.containsKey(currentTag)) {
                            channel = CHANNEL_TAGS.get(currentTag);

                            List<Item> items = contentStore.getItemsOnNow(channel.getUri());
                            if (!items.isEmpty()) {
                                item = items.get(0);
                                break;
                            }
                        }
                    }
                }
            }

            if (item != null) {

                Maybe<Consumption> consumption = Converters.fromItem(userRef, item, channel);
                if (consumption.hasValue()) {
                    consumptionStore.store(consumption.requireValue());
                }
            }
        }
    }

    private boolean isRelevant(String text) {
        return text.contains("watch") || text.contains("gawp") || text.contains("hark") || text.contains("gander") || text.contains("notice") || text.contains("observe") || text.contains("check out")
                || text.contains("stare");
    }
}
