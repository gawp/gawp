$(document).ready(function() {
    registerChannelClicks();
    registerConsumptionHovers();
});

var registerChannelClicks = function() {
	$(".channelLink").click(function() {
	var channel = $(this).attr('uri');
	$(".success").remove();
	$(".error").remove();
	    
	$.post("/watch.json", { channel: channel }, function(data) {
	    if (data.success) {
	        $(".container").append($('<p class="success">'+data.success+'</p>'));
	    } else if (data.error) {
	        $(".container").append($('<p class="error">'+data.error+'</p>'));
	        }
	    });
	    return false;
	});
}

var registerConsumptionHovers = function() {
	$(".consumption").hover(
	   function() { $(this).css('background', '#EBEBEB'); },
	   function() { $(this).css('background', 'white'); }
    );
};