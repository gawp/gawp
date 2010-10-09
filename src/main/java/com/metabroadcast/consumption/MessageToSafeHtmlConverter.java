package com.metabroadcast.consumption;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.util.HtmlUtils;

import com.google.common.collect.Lists;

public class MessageToSafeHtmlConverter {

	private static final Collection<String> tlds = Lists.newArrayList("\\.co\\.uk", "\\.com", "\\.org", "\\.net", "\\.tv", "\\.fr", "\\.gov", "\\.ac\\.uk", "\\.us", "\\.ly", "\\.to", "\\.eu", "\\.de", "\\.info", "\\.mobi");
	private static final Pattern URL_PATTERN = Pattern.compile(startingWithHttpOrWww() + endingWithMostTlds() + twitterReply());
	private String replacementForTwitterUsernames;
	
	private static String startingWithHttpOrWww() {
		return "(\\(?(http://|www\\.)[-A-Za-z0-9+&@#/%?=~_()|:,.;]*[-A-Za-z0-9+&@#/%?;=~_()|])";
	}
	
	private static String endingWithMostTlds() {
		String tldRe = "";
		for (String tld : tlds) {
			tldRe += "|(\\(?((?<!www\\.|http://)[-A-Za-z0-9+&@#/%?=~_()|:,.;])+" + tld + "[-A-Za-z0-9+&@#/%?=~_()|:,;]*)";
		}
		return tldRe;
	}
	
	private static String twitterReply() {
		return "|(\\(?@[A-Za-z0-9_]+\\)?)";
	}


	public String convert(final String message) {
		return exec(message, new UrlListener<String>() {

			private String encoded = HtmlUtils.htmlEscape(message);
			
			@Override
			public void twitterReply(String url) {
				if (replacementForTwitterUsernames != null) {
					 String username = url.substring(1);
					 String userUrl = String.format(replacementForTwitterUsernames, username);
					 encoded = encoded.replace(url, "@" + link(userUrl, username));
				 }
			}

			@Override
			public void url(String url) {
				encoded = encoded.replace(url, link(url, url));
			}

			@Override
			public String result() {
				return encoded;
			}
			
		});
	}
	
	public List<String> extractUrls(String message) {
		return exec(message, new UrlListener<List<String>>() {

			private final List<String> urls = Lists.newArrayList();
			
			@Override
			public List<String> result() {
				return urls;
			}

			@Override
			public void twitterReply(String user) {
				// ignore
			}

			@Override
			public void url(String url) {
				urls.add(cleanupUri(url));
			}
		});
	}
	
	private <T> T exec(String message, UrlListener<T> listener) {
		String encoded = HtmlUtils.htmlEscape(message);
		Matcher matcher = URL_PATTERN.matcher(encoded);
		while(matcher.find()) {
			String url = matcher.group();
			if (url.startsWith("(") && url.endsWith(")")) {
				url = url.substring(1, url.length() - 1);
			}
			if (url.startsWith("@")) {
				 listener.twitterReply(url);
			} else {
				listener.url(url);
			}
		}
		return listener.result();
	}
	
	private static interface UrlListener<T> {
		
		void url(String url);
		
		void twitterReply(String user);
		
		T result();
	}

	private String link(String url, String linkBody) {
		return "<a href=\"" + cleanupUri(url) + "\">" + linkBody +  "</a>";
	}

	private String cleanupUri(String url) {
		return url.startsWith("http://") || url.startsWith("/") ?  url : "http://" + url;
	}

	public MessageToSafeHtmlConverter withLinkTwitterRepliesTo(String twitterLink) {
		this.replacementForTwitterUsernames = twitterLink;
		return this;
	}
}
