var existingOnPlay = Events.Player.onPlay;

Events.Player.onPlay = function() {
    console.log('play event being sent');
    
    var event = document.createEvent("Event");
    event.initEvent('play', true, true);
    document.getElementById('mb_beige_comm').dispatchEvent(event);
    existingOnPlay();
};
