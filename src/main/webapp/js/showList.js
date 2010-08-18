goog.provide('blue.list');

blue.list.page = {};
blue.list.page.chart = {};

$(document).ready(function() {
	$('.periodSelect').change(function() {
		var period = $(this).attr('id');
		var index = $(this).val();
		
		window.location = "?period=" + period + "&index=" + index + "&chartType=" + blue.list.page.chart.type + '&target=' + blue.list.page.chart.target;
	});
	
	$('.chartTypeSelect').change(function() {
		window.location = "?period=" + blue.list.page.period + "&index=" + blue.list.page.index + "&chartType=" + $(this).val() + '&target=' + blue.list.page.chart.target;
	});
	
	$('.targetSelect').change(function() {
		var values = [];
		
		$('.targetSelect:checked').each(function() {
			values.push($(this).val());
		});
		
		var target = values.join(",");
		
		blue.list.page.chart.target = target;
		
		$('#chart').attr('src', '/stats/chart?period=' + blue.list.page.period + '&index=' + blue.list.page.index + '&type=' + blue.list.page.chart.type + 
											'&target=' + target + '&width=' + blue.list.page.chart.width + '&height=' + blue.list.page.chart.height);
	});
});

