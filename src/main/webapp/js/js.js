$(document).ready(function() {
    registerChannelClicks();
    registerBrandClicks();
    registerConsumptionHovers();
    registerSearchAutocomplete();
    registerOverviewLinks();
    registerRemoveLinks();
    
    if((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i))) { 
    	$(".subtitle").remove();
        $(".tabbar").iTabs();
    } else {
    	$(".footer").remove();
    	$(".module").removeClass('clearfix');
    }
});

var registerOverviewLinks = function() {
    $("#overview").hover(
        function() { $(this).children("a").css('color', '#71B7E6'); },
        function() { $(this).children("a").css('color', 'darkgrey'); }
    );
}

var triggerBookmarklet = function(url) {
	var bootstrap=document.createElement('script');
    bootstrap.src='/bookmark/bootstrap?uri='+encodeURIComponent(url);
    document.getElementsByTagName('head')[0].appendChild(bootstrap);
}

var uriLookup = {};
var registerSearchAutocomplete = function() {
	$("input#searchBox").autocomplete({
	    source: function (req, add) {
	    	$.getJSON("http://atlasapi.org/2.0/brands.json?callback=?&limit=5", {'title': req.term}, function(data) {
	    		var suggestions = []; 
	    		
	    		$.each(data.playlists, function(i, val){  
	    			uriLookup[val.title] = val.uri;
                    suggestions.push({'label': val.title, 'value': val.title});  
                });
                
                add(suggestions);
	    	});
	    },
	    select: function(e, ui) { 
	       var uri = uriLookup[ui.item.label];
	       if (uri) {
		       $("#searchBox").ajaxError(function() {
		           searchResponse(false);
		       });
	           $.post("/watch.json", { uri: uri }, function () {
	           	   searchResponse(true);
	           });
	       }
	    }
	});
}

var searchResponse = function (success) {
    var resultImage = (success ? '/images/tick.png' : '/images/cross.png');
    $.loading(true, { img:resultImage, align:'center'});
    setTimeout(function() {
        $.loading();
        $("#searchBox").unbind('ajaxError');
    }, 300);
}

var registerChannelClicks = function() {
	$(".channelLink").click(function() {
		var target = $(this);
		var image = $(this).find('img');
		image.css('opacity', '0.3');
		target.loading(true, { img:'/images/ajax-loader.gif', align:'center'});
		var channel = target.attr('uri');
		    
		target.ajaxError(function() {
            watchResponse(target, image, false);
        });
		$.post("/watch.json", { channel: channel }, 
	        function() {
			    watchResponse(target, image, true);
	        }
		);
	    return false;
	});
}

var registerBrandClicks = function() {
    $(".brandLink").click(function() {
        var target = $(this);
        var image = $(this).find('img');
        image.css('opacity', '0.3');
        target.loading(true, { img:'/images/ajax-loader.gif', align:'center'});
        var uri = target.attr('uri');
            
        target.ajaxError(function() {
            watchResponse(target, image, false);
        });
        $.post("/watch.json", { uri: uri }, 
            function() {
                watchResponse(target, image, true);
            }
        );
        return false;
    });
}

var watchResponse = function (target, image, success) {
	var resultImage = (success ? '/images/tick.png' : '/images/cross.png');
	target.loading();
    target.loading(true, { img:resultImage, align:'center'});
    setTimeout(function() {
        target.loading();
        image.css('opacity', '1');
        target.unbind('ajaxError');
    }, 300);
}

var registerConsumptionHovers = function() {
	$(".consumption").hover(
	   function() {
	   	 var $current = $(this);
	   	 var $children = $current.find('.title a');
	   	 $current.css('background', '#EBEBEB');
	   	 $children.css('color', '#0099FF');
	   	 $current.find('.ago').hide();
	   	 $current.find('.remove').show();
	   },
	   function() {
	   	 var $current = $(this);
	   	 var $children = $current.find('.title a');
	   	 $current.css('background', 'white'); 
	   	 $children.css('color', 'black');
	   	 $current.find('.ago').show();
         $current.find('.remove').hide();
	   }
    );
};

var registerRemoveLinks = function() {
	$('.remove_item').click(function() {
		var $target = $(this);
		
		$target.ajaxError(function() {
            alert('error removing item');
        });
		
		$.post('/remove', { uri: $target.attr('uri') },
		  function() {
		  	$target.parents('.consumption').slideUp(200, this.remove);
		  }
		);
    	return false;
	});
}