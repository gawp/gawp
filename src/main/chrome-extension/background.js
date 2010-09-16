var atlasUrl = "http://atlasapi.org/2.0/";
var playerHookUrl = "http://gawp.metabroadcast.com/js/browser-hooks/";
var consumeUrl = "http://gawp.metabroadcast.com/watch";
var beigeCookieUrl = 'http://gawp.metabroadcast.com/';
var beigeCookieName = 'beige';
var loggedOutIcon = 'chrome-ext-play-button-logged-out.png';

chrome.extension.onRequest.addListener(receiveMessage);

function receiveMessage(request, sender, callback) {
	if (request.msg == "checkNewLocation") {
	    if (!getPageHook(sender.tab.url)) {
	        checkForItemThenBrand(sender.tab);
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
	    $.ajax({
	        url: consumeUrl,
	        data: {'uri': sender.tab.url},
	        type: 'POST',
	        success: function(data) {
	            console.log("consume item success");
	            chrome.pageAction.hide(sender.tab.id);
	        },
	        error: function(requestObject, textStatus, errorThrown) {
	            console.log("consume item error");
	            chrome.pageAction.show(sender.tab.id);
	            
	            /*callback({'consumeUrl': consumeUrl,
	                      'itemUrl': sender.tab.url});*/
	        }
	    });
    }
}

function checkLoginStatus(tab) {
    chrome.cookies.get({'url': beigeCookieUrl, 'name': beigeCookieName}, function(cookie) {
        if (cookie && cookie.value.substring(0, 2) != "an") {
            console.log('found beige cookie');
        }
        else {
            console.log('could not find beige cookie');
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
    var itemReqUrl = atlasUrl + "items.json?uri=" + tab.url;
    console.log("Requesting items: " + itemReqUrl);
    
    $.ajax({url: itemReqUrl,
        dataType: 'json',
        success: function(data) {
            if (data) {
                console.log("Found " + data.items.length + " items");
                
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
            console.log('error getting item');
            checkForBrand(tab);
        }
    });
}

function checkForBrand (tab) {
	var brandReqUrl = atlasUrl + "brands.json?uri=" + tab.url;
	console.log("Requesting brand: " + brandReqUrl);
	
	$.ajax({url: brandReqUrl,
		dataType: 'json',
		success: function(data) {
			if (data) {
				console.log("Found " + data.playlists.length + " brands");
				
				if (data.playlists.length > 0) {
				    chrome.pageAction.show(tab.id);
				}
			}
		},
		error: function(requestObject, textStatus, errorThrown) {
			console.log('error checking for brand');
		}
	});
}


