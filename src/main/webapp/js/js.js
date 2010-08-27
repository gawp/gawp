$(document).ready(function() {
    registerChannelClicks();
    registerBrandClicks();
    registerConsumptionHovers();
    $(".tabbar").iTabs();
});

var registerSearchAutocomplete = function() {
	$("input#searchBox").autocomplete({
	    source: ["c++", "java", "php", "coldfusion", "javascript", "asp", "ruby"]
	});
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
	   function() { $(this).css('background', '#EBEBEB'); },
	   function() { $(this).css('background', 'white'); }
    );
};