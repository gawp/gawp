package com.metabroadcast;

import static org.junit.Assert.*;

import java.util.List;

import org.atlasapi.client.CachingJaxbAtlasClient;
import org.atlasapi.media.entity.simple.Item;
import org.junit.Test;

import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.content.AtlasContentStore;
import com.metabroadcast.content.ContentStore;


public class AtlasContentStoreTest {

    { Configurer.load(); }
    
    private ContentStore store = new AtlasContentStore(new CachingJaxbAtlasClient().withApiKey(Configurer.get("atlas.apikey").get()));
    
    @Test
    public void shouldRetrieveItemsOnNow() {
        List<Item> items = store.getItemsOnNow("http://www.bbc.co.uk/services/bbcone/london");
        assertFalse(items.isEmpty());
    }
}
