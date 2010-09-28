package com.metabroadcast.consumption.www;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.BrandSummary;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.model.SimpleModel;
import com.metabroadcast.common.social.model.TargetRef;
import com.metabroadcast.common.social.model.TwitterUserDetails;
import com.metabroadcast.common.social.model.UserDetails;
import com.metabroadcast.common.social.model.UserRef;
import com.metabroadcast.common.social.model.UserRef.UserNamespace;
import com.metabroadcast.common.social.user.UserDetailsProvider;
import com.metabroadcast.common.social.user.UserProvider;
import com.metabroadcast.common.stats.Count;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.consumption.ConsumedContent;
import com.metabroadcast.consumption.ConsumedContentProvider;
import com.metabroadcast.consumption.Consumption;
import com.metabroadcast.consumption.ConsumptionStore;
import com.metabroadcast.content.Channel;
import com.metabroadcast.content.ContentRefs;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SeriesOrder;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;
import com.metabroadcast.content.SimplePlaylistAttributesModelBuilder;
import com.metabroadcast.neighbours.Neighbour;
import com.metabroadcast.neighbours.NeighboursProvider;
import com.metabroadcast.user.twitter.TwitterUserRefProvider;

@Controller
public class ConsumptionController {

    private final static int MAX_RECENT_ITEMS = 10;
    private final static int MAX_TOP_BRANDS = 8;
    private final static int MAX_GRAPH_ROWS = 6;

    private final ConsumptionStore consumptionStore;
    private final ConsumedContentProvider consumedContentProvider;

    private final ModelListBuilder<ConsumedContent> consumedContentModelListBuilder = DelegatingModelListBuilder.delegateTo(new ConsumedContentModelBuilder(new SimplePlaylistAttributesModelBuilder(),
            new SimpleItemAttributesModelBuilder()));
    
    private final ContentStore contentStore;
    private final UserProvider userProvider;
    private final UserDetailsProvider userDetailsProvider;
    private final TwitterUserRefProvider userRefProvider;
    private final NeighboursProvider neighboursProvider;
    
    private final Log log = LogFactory.getLog(getClass());

    public ConsumptionController(ConsumptionStore consumptionStore, ContentStore contentStore, UserProvider userProvider, UserDetailsProvider userDetailsProvider,
            TwitterUserRefProvider userRefProvider, NeighboursProvider neighboursProvider) {
        this.consumptionStore = consumptionStore;
        this.contentStore = contentStore;
        this.userDetailsProvider = userDetailsProvider;
        this.userRefProvider = userRefProvider;
        this.neighboursProvider = neighboursProvider;
        this.consumedContentProvider = new ConsumedContentProvider(consumptionStore, contentStore);
        this.userProvider = userProvider;
    }

    @RequestMapping(value = { "/{user}" }, method = { RequestMethod.GET })
    public String watches(@PathVariable String user, Map<String, Object> model) {

        Maybe<UserRef> userRef = userRefProvider.ref(user);
        UserRef currentUserRef = userProvider.existingUser();

        Maybe<UserDetails> userDetails = getUserDetails(currentUserRef);
        model.put("currentUserDetails", userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));

        return watches(model, userRef.requireValue());
    }

    @RequestMapping(value = { "/watches", "/" }, method = { RequestMethod.GET })
    public String userWatches(Map<String, Object> model, HttpServletRequest request) {
        
        String useragent = request.getHeader("User-Agent");
        if (useragent != null && useragent.contains("iPhone")) {
            return "redirect:/watch";
        }
        
        UserRef userRef = userProvider.existingUser();

        if (userRef.isInNamespace(UserNamespace.TWITTER)) {
            Maybe<UserDetails> userDetails = getUserDetails(userRef);

            if (userDetails.hasValue()) {
                return "redirect:/" + userDetails.requireValue().getScreenName();
            }
        }

        model.put("currentUserDetails", null);

        return allWatches(model);
    }
    
    @RequestMapping(value = { "/all" }, method = { RequestMethod.GET })
    public String all(Map<String, Object> model) {
        UserRef userRef = userProvider.existingUser();

        model.put("currentUserDetails", userRef);

        return allWatches(model);
    }
    
    private String allWatches(Map<String, Object> model) {
        UserRef currentUserRef = userProvider.existingUser();

        Maybe<UserDetails> userDetails = getUserDetails(currentUserRef);
        model.put("currentUserDetails", userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        
        List<ConsumedContent> consumedContent = consumedContentProvider.find(null, MAX_RECENT_ITEMS);
        model.put("items", consumedContentModelListBuilder.build(consumedContent));
        
        List<Consumption> consumptions = consumptionStore.find(null, new DateTime(DateTimeZones.UTC).minusWeeks(4));
        
        List<Count<String>> brands = consumedContentProvider.findBrandCounts(consumptions);
        if (brands.size() > MAX_TOP_BRANDS) {
            brands = brands.subList(0, MAX_TOP_BRANDS);
        }
        addBrandCountsModel(model, brands);
        
        List<Count<String>> channels = consumedContentProvider.findChannelCounts(consumptions);
        addChannelCountsModel(model, channels);
        
        List<Count<String>> genres = consumedContentProvider.findGenreCounts(consumptions);
        addGenreCountsModel(model, genres);
        
        addOverviewModel(model, channels, genres, consumptions, null);
        
        return "watches/home";
    }

    private String watches(Map<String, Object> model, UserRef userRef) {
        long getUser = System.currentTimeMillis();

        Maybe<UserDetails> userDetails = getUserDetails(userRef);
        model.put("userDetails", userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));

        long getUserDetails = System.currentTimeMillis();

        Preconditions.checkNotNull(userRef);

        List<ConsumedContent> consumedContent = consumedContentProvider.find(userRef, MAX_RECENT_ITEMS);
        model.put("items", consumedContentModelListBuilder.build(consumedContent));

        long getContent = System.currentTimeMillis();

        List<Consumption> consumptions = consumptionStore.find(userRef, new DateTime(DateTimeZones.UTC).minusWeeks(4));

        long getConsumptions = System.currentTimeMillis();

        List<Count<String>> brands = consumedContentProvider.findBrandCounts(consumptions);
        if (brands.size() > MAX_TOP_BRANDS) {
            brands = brands.subList(0, MAX_TOP_BRANDS);
        }
        addBrandCountsModel(model, brands);

        long getBrands = System.currentTimeMillis();

        List<Count<String>> channels = consumedContentProvider.findChannelCounts(consumptions);
        addChannelCountsModel(model, channels);

        long getChannels = System.currentTimeMillis();

        List<Count<String>> genres = consumedContentProvider.findGenreCounts(consumptions);
        addGenreCountsModel(model, genres);

        long getGenres = System.currentTimeMillis();
        
        addOverviewModel(model, channels, genres, consumptions, (TwitterUserDetails) userDetails.valueOrNull());
        
        addNeighboursModel(model, userRef);
        
        long getNeighbours = System.currentTimeMillis();

        if (log.isInfoEnabled()) {
            log.info("Get user details: " + (getUserDetails - getUser) + ", get content: " + (getContent - getUserDetails) + " get consumptions: " + (getConsumptions - getContent) + ", get brands: "
                    + (getBrands - getConsumptions) + ", get channels: " + (getChannels - getBrands) + ", get genres: " + (getBrands - getGenres) + ", get neighbours: "+(getNeighbours - getGenres));
        }

        return "watches/list";
    }

    private Maybe<UserDetails> getUserDetails(UserRef userRef) {
        try {
            Map<UserRef, UserDetails> userDetailsMap = userDetailsProvider.detailsFor(userRef, Lists.newArrayList(userRef));
            if (userDetailsMap.containsKey(userRef)) {
                return Maybe.fromPossibleNullValue(userDetailsMap.get(userRef));
            }
        } catch (Exception e) {
            log.warn("unable to get user details", e);
        }
        return Maybe.nothing();
    }

    @RequestMapping(value = { "/watch" }, method = { RequestMethod.POST })
    public void watch(HttpServletResponse response, @RequestParam(required = false, value="channel") String channelUri, @RequestParam(required = false) String uri, Map<String, Object> model) {
       
    	Channel channel = channelUri == null ? null : Channel.fromUri(channelUri);
    	
    	UserRef userRef = userProvider.existingUser();
    	if (! userRef.isInNamespace(UserNamespace.TWITTER)) {
    	    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    	    return;
    	}

        Item item = null;

        if (uri != null) {
            Maybe<Description> description = contentStore.resolve(uri);
            if (description.hasValue()) {
                if (description.requireValue() instanceof Playlist) {
                    Playlist playlist = (Playlist) description.requireValue();
                    List<Item> items = playlist.getItems();

                    if (!items.isEmpty()) {
                        Collections.sort(items, new SeriesOrder());
                        Collections.reverse(items);
                        item = items.get(0);
                    }
                } else if (description.requireValue() instanceof Item) {
                    item = (Item) description.requireValue();
                }
            }
            if (channel == null && item != null && item.getPublisher() != null) {
                Maybe<Publisher> publisher = Publisher.fromKey(item.getPublisher().getKey());
                if (publisher.hasValue()) {
                    Channel c = Channel.onlineChannelForPublisher(publisher.requireValue());
                    if (c != null) {
                        channel = c;
                    }
                }
            }
        } else if (channel != null) {
            List<Item> items = contentStore.getItemsOnNow(channel.getUri());
            if (!items.isEmpty()) {
                item = items.get(0);
            }
        }

        if (item != null) {
            BrandSummary brand = item.getBrandSummary();
            PublisherDetails publisher = item.getPublisher();
            TargetRef targetRef = new TargetRef(item.getUri(), ContentRefs.ITEM_DOMAIN);
            Set<String> genres = genres(item);

            consumptionStore.store(new Consumption(userRef, targetRef, new DateTime(DateTimeZones.UTC), channel, publisher != null ? publisher.getKey() : null, brand != null ? brand.getUri() : null,
                    genres));
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            model.put("error", "Unfortunately, there's nothing on that channel");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @RequestMapping(value = { "/remove" }, method = { RequestMethod.POST })
    public void unwatch(HttpServletResponse response, @RequestParam(required = true) String uri) {
        UserRef userRef = userProvider.existingUser();
        Preconditions.checkNotNull(userRef);
        
        consumptionStore.remove(userRef, new TargetRef(uri, ContentRefs.ITEM_DOMAIN));
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private Set<String> genres(Item item) {
        Set<String> genres = Sets.newHashSet();
        for (String genre : item.getGenres()) {
            if (genre.startsWith("http://ref.atlasapi.org")) {
                genres.add(genre);
            }
        }
        return genres;
    }

    @RequestMapping(value = { "/watch" }, method = { RequestMethod.GET })
    public String watchOptions(Map<String, Object> model) {
        long start = System.currentTimeMillis();

        model.put("channels", Channel.mapListWithoutVodServices());

        long getUser = System.currentTimeMillis();

        UserRef userRef = userProvider.existingUser();
        Maybe<UserDetails> userDetails = getUserDetails(userRef);
        model.put("userDetails", userDetailsModel((TwitterUserDetails) userDetails.valueOrNull()));
        model.put("loggedIn", !userRef.getNamespace().equals(UserNamespace.ANONYMOUS));

        long getBrands = System.currentTimeMillis();

        List<Consumption> consumptions = consumptionStore.find(userRef, new DateTime(DateTimeZones.UTC).minusWeeks(4));
        List<Count<String>> brands = consumedContentProvider.findBrandCounts(consumptions);

        long getMoreBrands = System.currentTimeMillis();

        if (brands.size() < 12) {
            Map<String, Count<String>> topBrands = consumptionStore.topBrands(20);
            for (Count<String> brand : topBrands.values()) {
                if (!brands.contains(brand)) {
                    brands.add(brand);
                }
            }
        }

        if (brands.size() > 12) {
            brands = brands.subList(0, 12);
        }
        addBrandCountsModel(model, brands);

        long end = System.currentTimeMillis();

        if (log.isInfoEnabled()) {
            log.info("Get channels: " + (getUser - start) + ", get user: " + (getBrands - getUser) + ", get brands: " + (getMoreBrands - getBrands) + ", get more brands: " + (end - getMoreBrands));
        }

        return "watches/watch";
    }

    private int max(List<Count<String>> counts) {
        int max = 0;
        for (Count<?> count : counts) {
            int current = Long.valueOf(count.getCount()).intValue();
            if (current > max) {
                max = current;
            }
        }

        return max;
    }
    
    private void addNeighboursModel(Map<String, Object> model, UserRef userRef) {
        List<Map<String, ?>> users = Lists.newArrayList();
        
        List<UserRef> userRefs = Lists.newArrayList(Iterables.transform(neighboursProvider.neighbours(userRef), neighbourToUserRef));
        Map<UserRef, UserDetails> userDetails = userDetailsProvider.detailsFor(null, userRefs);
        
        for (UserDetails details: userDetails.values()) {
            users.add(this.userDetailsModel((TwitterUserDetails) details).asMap()); 
        }
        
        model.put("neighbours", users);
    }
    
    private static final Function<Neighbour, UserRef> neighbourToUserRef = new Function<Neighbour, UserRef>() {
        @Override
        public UserRef apply(Neighbour n) {
            return n.neighbour();
        }
    };

    private void addBrandCountsModel(Map<String, Object> model, List<Count<String>> brands) {
        List<Map<String, Object>> counts = Lists.newArrayList();
        int max = max(brands);
        model.put("max", max);

        for (Count<String> count : brands) {
            Map<String, Object> countMap = Maps.newHashMap();
            int countVal = Long.valueOf(count.getCount()).intValue();
            countMap.put("count", Long.valueOf(count.getCount()).intValue());
            countMap.put("width", Float.valueOf((float) countVal / (float) max * 100).intValue());

            Maybe<Description> desc = contentStore.resolve(count.getTarget());
            Map<String, Object> targetMap = Maps.newHashMap();
            if (desc.hasValue()) {
                Description brand = desc.requireValue();
                targetMap.put("title", brand.getTitle());
                targetMap.put("uri", brand.getUri());
                targetMap.put("logo", brand.getThumbnail());
                targetMap.put("link", brand.getUri());
            }

            countMap.put("target", targetMap);
            counts.add(countMap);
        }
        model.put("brands", counts);
    }

    private void addChannelCountsModel(Map<String, Object> model, List<Count<String>> channels) {
        List<Map<String, Object>> counts = Lists.newArrayList();
        int max = max(channels);
        model.put("max", max);

        for (Count<String> count : Iterables.limit(channels, MAX_GRAPH_ROWS)) {
            Map<String, Object> countMap = Maps.newHashMap();
            int countVal = Long.valueOf(count.getCount()).intValue();
            countMap.put("count", Long.valueOf(count.getCount()).intValue());
            countMap.put("width", Float.valueOf((float) countVal / (float) max * 100).intValue());

            Map<String, Object> targetMap = Maps.newHashMap();
            Channel channel = Channel.fromUri(count.getTarget());
            if (channel != null) {
                targetMap.put("title", channel.getName());
                targetMap.put("uri", channel.getUri());
                targetMap.put("logo", channel.getLogo());
                targetMap.put("link", channel.getUri());
            }

            countMap.put("target", targetMap);
            counts.add(countMap);
        }
        model.put("channels", counts);
    }

    private void addGenreCountsModel(Map<String, Object> model, List<Count<String>> genres) {
        List<Map<String, Object>> counts = Lists.newArrayList();
        int max = max(genres);
        model.put("max", max);

        for (Count<String> count : Iterables.limit(genres, MAX_GRAPH_ROWS)) {
            Map<String, Object> countMap = Maps.newHashMap();
            int countVal = Long.valueOf(count.getCount()).intValue();
            countMap.put("count", Long.valueOf(count.getCount()).intValue());
            countMap.put("width", Float.valueOf((float) countVal / (float) max * 100).intValue());

            Map<String, Object> targetMap = Maps.newHashMap();
            targetMap.put("title", ConsumedContentProvider.GENRE.apply(count.getTarget()));
            targetMap.put("uri", count.getTarget());

            countMap.put("target", targetMap);
            counts.add(countMap);
        }
        model.put("genres", counts);
    }

    public SimpleModel userDetailsModel(TwitterUserDetails userDetails) {
        SimpleModel model = new SimpleModel();
        if (userDetails != null) {
            String targetName = userDetails.getFullName() != null ? userDetails.getFullName() : userDetails.getScreenName();
            String possessivePostfix = targetName.endsWith("s") ? "'" : "'s";
            
            model.put("screenName", userDetails.getScreenName());
            model.put("fullName", userDetails.getFullName());
            model.put("possessivePostfix", possessivePostfix);
            model.put("followers", userDetails.getFollowerCount());
            model.put("profileImage", userDetails.getProfileImage());
            model.put("profileUrl", userDetails.getProfileUrl());
            model.put("bio", userDetails.getBio());
            model.put("location", userDetails.getLocation());
            model.put("id", userDetails.getUserRef().getUserId());
        }
        return model;
    }

    private void addOverviewModel(Map<String, Object> model, List<Count<String>> channels, List<Count<String>> genres, List<Consumption> consumptions, TwitterUserDetails userDetails) {
        Map<String, Object> overview = Maps.newHashMap();

        Channel topChannel = null;
        String recentContent = null;
        String topGenre = null;

        if (!channels.isEmpty()) {
            topChannel = Channel.fromUri(channels.get(0).getTarget());

            for (Consumption consumption : consumptions) {
                if (consumption.getChannel() != null && consumption.getChannel().equals(topChannel.getUri())) {
                    recentContent = consumption.getBrandUri();
                    break;
                }
            }
        }
        if (!genres.isEmpty()) {
            topGenre = genres.iterator().next().getTarget();
        }

        Map<String, Object> targetMap = Maps.newHashMap();
        if (recentContent != null) {
            Maybe<Description> desc = contentStore.resolve(recentContent);
            if (desc.hasValue()) {
                Description brand = desc.requireValue();
                targetMap.put("title", brand.getTitle());
                targetMap.put("uri", brand.getUri());
                targetMap.put("logo", brand.getThumbnail());
                targetMap.put("link", brand.getUri());
            }
        }
        overview.put("target", targetMap);

        targetMap = Maps.newHashMap();
        if (topChannel != null) {
            targetMap.put("title", topChannel.getName());
            targetMap.put("uri", topChannel.getUri());
            targetMap.put("logo", topChannel.getLogo());
            targetMap.put("link", topChannel.getUri());
        }
        overview.put("channel", targetMap);
        
        targetMap = Maps.newHashMap();
        if (topGenre != null) {
            targetMap.put("title", ConsumedContentProvider.GENRE.apply(topGenre));
            targetMap.put("uri", topGenre);
        }
        overview.put("genre", targetMap);
        
        overview.put("userDetails", userDetailsModel(userDetails).asMap());
        
        model.put("overview", overview);
    }
}
