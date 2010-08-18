goog.provide('blue.login');

var getUrlVars = function() {
	var vars = {};
	
	var hashes = window.location.search.slice(1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
        var hash = hashes[i].split('=');
        vars[hash[0]] = hash[1];
    }
    return vars;
};

var targetUri = getUrlVars().targetUri;

var redirectToTarget = function() {
	if (targetUri) {
		window.location = targetUri;
	}
	else {
		window.location = "/";
    }
}

$(document).ready(function() {
	var urlVars = getUrlVars();
	
	twttr.anywhere(function (T) {
		if (window.location.pathname == "/login") {
			if (T.isConnected()) {
				redirectToTarget();
			}
			else {
				T("#login").connectButton({ size:      		"large",
	    									authComplete: 	function(user) {
	    														redirectToTarget();
	    													}});
			}
		}
		
		T.bind("signOut", function(e) {
			window.location = "/goodbye";
		});
  	});
  	
  	$('#logoutButton').bind('click', function() {
  										 twttr.anywhere.signOut();
  									 });
});

