var consumeUrl = "http://beige.mbst.tv/watch";

var addHooks = function() {
    glow.events.addListener(document, 'iplayer:emp:pause', function(e) { 
        console.log('pause!');
    });
    glow.events.addListener(document, 'iplayer:emp:play', function(e) { 
        console.log('play event being sent');
    });
    glow.events.addListener(document, 'iplayer:emp:watched', function(e) {
        console.log('watched event being sent');
        var event = document.createEvent("Event");
        event.initEvent('play', true, true);
        document.getElementById('mb_beige_comm').dispatchEvent(event);
    });
};

if (glow) {
    if (glow.isReady) { 
        addHooks();
    }
    else { 
        glow.ready(addHook);
    }
}
