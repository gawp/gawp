var beigeUrl = 'http://gawp.metabroadcast.com/bookmark/iframe?uri=';

chrome.tabs.getSelected(null, function(tab) {
    var iframeHtml = '<iframe src="' + beigeUrl + encodeURIComponent(tab.url) + '" width="100%" height="100%" frameborder="0"></iframe>';

    $('body').append(iframeHtml);
});

