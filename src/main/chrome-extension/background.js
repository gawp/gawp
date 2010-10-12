var atlasUrl = "http://atlasapi.org/2.0/";
var playerHookUrl = "http://gawp.metabroadcast.com/js/browser-hooks/";
var consumeUrl = "http://gawp.metabroadcast.com/watch";
var beigeCookieUrl = 'http://gawp.metabroadcast.com/';
var beigeCookieName = 'beige';
var loggedOutIcon = 'chrome-ext-play-button-logged-out.png';
var errorIcon = 'chrome-ext-play-button-logged-out.png';
var whitelistUrl = 'http://gawp.metabroadcast.com/extension-data/whitelist.json';
var whitelist = {};

$.ajax({
    url: whitelistUrl,
    type: 'GET',
    dataType: 'json',
    success: function(data) {
        whitelist = data;
    },
    error: function(requestObject, textStatus, errorThrown) {
        console.log("error fetching gawp whitelist, gawping will not work until extension / browser is restarted");
        console.log(textStatus);
        console.log(errorThrown);
    }
});

chrome.extension.onRequest.addListener(receiveMessage);

function receiveMessage(request, sender, callback) {
	if (request.msg == "checkNewLocation") {
	    if (!getPageHook(sender.tab.url)) {
	        if (isWhitelisted(sender.tab.url)) {
	            checkForItemThenBrand(sender.tab);
	        }
	    }
	}
	else if (request.msg == "getPlayerHook") {
	    var hook = getPageHook(sender.tab.url);
	    if (hook) {
	        checkLoginStatus(sender.tab);
	        callback(playerHookUrl + hook);
	    }
	}
	else if(request.msg == "consumeItem") {
	    console.log('gawping item: ' + sender.tab.url);
	    $.ajax({
	        url: consumeUrl,
	        data: {'uri': sender.tab.url},
	        type: 'POST',
	        success: function(data) {
	            console.log("successfully gawped item: " + sender.tab.url);
	            chrome.pageAction.hide(sender.tab.id);
	        },
	        error: function(requestObject, textStatus, errorThrown) {
	            console.log("error when trying to gawp item: " + sender.tab.url);
	            chrome.pageAction.show(sender.tab.id);
	            
	            /*callback({'consumeUrl': consumeUrl,
	                      'itemUrl': sender.tab.url});*/
	        }
	    });
    }
}

function isWhitelisted(url) {
    for (var i in whitelist) {
        var whitelistedUrl = whitelist[i];
        if (url.indexOf(whitelistedUrl) != -1) {
            return true;
        }
    }
    
    return false;
}

function checkLoginStatus(tab) {
    chrome.cookies.get({'url': beigeCookieUrl, 'name': beigeCookieName}, function(cookie) {
        if (!cookie || cookie.value.substring(0, 2) == "an") {
            chrome.pageAction.setIcon({'tabId': tab.id, 'path': loggedOutIcon});
            chrome.pageAction.show(tab.id);
        }
    })
}

function getPageHook(url) {
    if (url.slice(0, "http://www.bbc.co.uk".length) == "http://www.bbc.co.uk") {
        return 'iplayer.js';
    }
    if (url.slice(0, "http://www.channel4.com/programmes/".length) == "http://www.channel4.com/programmes/") {
        return '4od.js';
    }
    return null;
}

function checkForItemThenBrand (tab) {
    console.log('sending query to atlas for page: ' + tab.url);
    
    var itemReqUrl = atlasUrl + "items.json?uri=" + tab.url;
    
    $.ajax({url: itemReqUrl,
        dataType: 'json',
        success: function(data) {
            if (data) {
                if (data.items.length > 0) {
                    chrome.pageAction.show(tab.id);
                }
                else {
                    checkForBrand(tab);
                }
            }
            else {
                checkForBrand(tab);
            }
            
        },
        error: function(requestObject, textStatus, errorThrown) {
            console.log('error when querying atlas for item: ' + tab.url);
            checkForBrand(tab);
        }
    });
}

function checkForBrand (tab) {
	var brandReqUrl = atlasUrl + "brands.json?uri=" + tab.url;
	
	$.ajax({url: brandReqUrl,
		dataType: 'json',
		success: function(data) {
			if (data) {
				if (data.playlists.length > 0) {
				    chrome.pageAction.show(tab.id);
				}
			}
		},
		error: function(requestObject, textStatus, errorThrown) {
			console.log('error when querying atlas for brand: ' + tab.url);
		}
	});
}


