package com.metabroadcast.consumption.www;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.social.model.TwitterUserDetails;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.consumption.ConsumedContent;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.user.www.UserModelHelper;

public class ConsumedContentModelBuilder implements ModelBuilder<ConsumedContent> {

    private final SimpleItemAttributesModelBuilder itemModelBuilder;
    private final SimplePlaylistAttributesModelBuilder playlistModelBuilder;
    private final UserModelHelper userModelHelper;

    public ConsumedContentModelBuilder(SimplePlaylistAttributesModelBuilder playlistModelBuilder, SimpleItemAttributesModelBuilder itemModelBuilder, UserModelHelper userModelHelper) {
        this.playlistModelBuilder = playlistModelBuilder;
        this.itemModelBuilder = itemModelBuilder;
        this.userModelHelper = userModelHelper;
    }

    @Override
    public SimpleModel build(ConsumedContent target) {
        SimpleModel model = new SimpleModel();

        if (target.getContent() instanceof Item) {
            model.put("content", itemModelBuilder.build((Item) target.getContent()));
        } else if (target.getContent() instanceof Playlist) {
            model.put("content", playlistModelBuilder.build((Playlist) target.getContent()));
        }

        model.put("count", Long.valueOf(target.getCount().getCount()).intValue());
        if (target.getConsumption().getPublisher() != null) {
            Maybe<Publisher> publisher = Publisher.fromKey(target.getConsumption().getPublisher());
            if (publisher.hasValue()) {
                model.put("publisher", publisher.requireValue().name());
            }
        }
        Channel channel = Channel.fromUri(target.getConsumption().getChannel());
        if (channel != null) {
            model.put("channel", channel.toModel());
        }
        model.put("ago", ago(target.getConsumption().timestamp()));
        if (target.getConsumption() != null && target.getConsumption().userRef() != null) {
            Maybe<UserDetails> userDetails = userModelHelper.getUserDetails(target.getConsumption().userRef());
            model.put("user", userModelHelper.userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        }

        return model;
    }

    public static String ago(DateTime then) {
        if (null == then)
            return "some time ago";

        DateTime now = new DateTime();
        Period period = new Interval(then, now).toPeriod();
        int printed = 0;

        StringBuffer ago = new StringBuffer();
        printed = printPeriod(ago, printed, period.getYears(), "year");
        printed = printPeriod(ago, printed, period.getMonths(), "month");
        printed = printPeriod(ago, printed, period.getWeeks(), "week");
        printed = printPeriod(ago, printed, period.getDays(), "day");
        printed = printPeriod(ago, printed, period.getHours(), "hour");
        printed = printPeriod(ago, printed, period.getMinutes(), "minute");

        if (printed > 0) {
            ago.append(" ago");
        } else {
            ago.append("just now");
        }
        return ago.toString();
    }

    private static int printPeriod(StringBuffer ago, int printed, int value, String desc) {
        if (printed > 0) {
            return printed;
        }

        if (value > 1) {
            if (printed == 1)
                ago.append(" and ");
            ago.append(value + " " + desc + "s");
        } else if (value == 1) {
            if (printed == 1)
                ago.append(" and ");
            ago.append(value + " " + desc);
        } else {
            return printed;
        }
        return printed + 1;
    }
}
