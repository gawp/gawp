var commElement = document.createElement("div");
commElement.id = 'mb_beige_comm';
commElement.style = 'display:none';
document.head.appendChild(commElement);

$('#mb_beige_comm').bind('play', function() {
    console.log('received play event!');
    chrome.extension.sendRequest({msg: 'consumeItem'}, function(failed) {
        if (failed) {
            var iframeHtml = '<iframe src="http://gawp.metabroadcast.com/bookmark?uri=' + encodeURIComponent(failed.itemUrl) + '" width="100%" height="100%" frameborder="0"></iframe>';
            
            $('body').prepend(iframeHtml);
        }
    });
});

chrome.extension.sendRequest({msg: 'getPlayerHook'}, 
    function(hookUrl) {
        if (hookUrl) {
            $('head').append('<script type="text/javascript" src="' + hookUrl + '"></script>');
        }
    }
);
