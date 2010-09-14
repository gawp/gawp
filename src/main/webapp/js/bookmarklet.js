(function(){
    var iframe=document.createElement('iframe');
    iframe.width=480;
    iframe.height=480;
    iframe.style.cssText="position:fixed; z-index: 1000; top: 0; right: 0; border: 10px 0 0 0 solid #aaa;";
    iframe.src='http://dev.mbst.tv:8080/bookmark?uri=' + encodeURIComponent(document.location);
    var closeButton=document.createElement('a');
    closeButton.href="#";
    closeButton.onclick = function() { 
        document.body.removeChild(iframe); 
        document.body.removeChild(closeButton); 
        return false; 
    };
    closeButton.innerHTML = "Close";
    closeButton.style.cssText="position:fixed; z-index: 1001; top: 10px; right: 20px;";
    document.body.appendChild(iframe);
    document.body.appendChild(closeButton);
})();