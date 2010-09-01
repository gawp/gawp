package com.metabroadcast;

import static org.junit.Assert.*;

import java.util.List;

import org.atlasapi.client.CachingJaxbAtlasClient;
import org.atlasapi.media.entity.simple.Item;
import org.junit.Test;

import com.metabroadcast.content.AtlasContentStore;
import com.metabroadcast.content.ContentStore;


public class AtlasContentStoreTest {

    private ContentStore store = new AtlasContentStore(new CachingJaxbAtlasClient());
    
    @Test
    public void shouldRetrieveItemsOnNow() {
        List<Item> items = store.getItemsOnNow("http://www.bbc.co.uk/services/parliament");
        assertFalse(items.isEmpty());
    }
}
