package com.metabroadcast.beige.bookmarklet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.model.DelegatingModelListBuilder;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.ModelListBuilder;
import com.metabroadcast.common.url.UrlEncoding;
import com.metabroadcast.content.ContentStore;
import com.metabroadcast.content.SimpleItemAttributesModelBuilder;

@Controller
public class BookmarkletController {

	private final ModelBuilder<Item> itemModelBuilder = new SimpleItemAttributesModelBuilder();
	private final ModelListBuilder<Item> itemsModelBuilder = DelegatingModelListBuilder.delegateTo(itemModelBuilder); 

	private final ContentStore contentStore;

	public BookmarkletController(ContentStore contentStore) {
		this.contentStore = contentStore;
	}
	
	@RequestMapping("/bookmark/iframe")
	public String showBookmarklet(@RequestParam("uri") String uri, Map<String, Object> model) {
		Description content = contentFor(uri);
		model.put("uri", uri);
		
		if (content == null) {
			return "bookmarklet/notfound";
		}
		
		if (content instanceof Playlist) {
			Playlist playlist = (Playlist) content;
			model.put("items", itemsModelBuilder.build(playlist.getItems()));
			return "bookmarklet/brand";
		}
		
		model.put("item", itemModelBuilder.build((Item) content));
		return "bookmarklet/item";
	}
	
	@RequestMapping("/bookmark/bootstrap.js")
	public String showBootrap(Map<String, Object> model, @RequestParam("uri") String uri, HttpServletRequest request) {
		Description content = contentFor(uri);

		if (content == null) {
			model.put("frameHeight", 210);
		} else if (content instanceof Playlist) {
			Playlist playlist = (Playlist) content;
			model.put("frameHeight", 210 + playlist.getItems().size() * 53);
		} else {
			model.put("frameHeight", 275);
		}
		model.put("frameSrc", hostFrom(request.getRequestURL())  + "/bookmark/iframe?uri=" + UrlEncoding.encode(uri));
		return "bookmarklet/bootstrap";
	}
	
	private String hostFrom(StringBuffer requestURL) {
		String s = requestURL.toString();
		int protocolSlashes = s.indexOf("//");
		int idx = s.indexOf('/', protocolSlashes + 2);
		if (idx > 0) {
			return s.substring(0, idx); 
		}
		return s;
	}

	private Description contentFor(String uri) {
		Map<String, Description> found = contentStore.resolveAll(ImmutableList.of(uri));
		if (found.isEmpty()) {
			return null;
		} 
		return Iterables.getOnlyElement(found.values());
	}
}
