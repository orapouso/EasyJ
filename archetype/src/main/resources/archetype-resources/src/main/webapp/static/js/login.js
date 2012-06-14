var AppMain = AppMain || {};
AppMain.Login = function() {
    var state = location.search.substring(1);
    if(state != "") {
        new Effect.Opacity("loginError", {
            from:0,to:1,
            afterSetup: function(effect) {effect.element.show();},
            afterFinish: function() {
                new Effect.Highlight("loginError", {queue:{position:"end", scope:"loginError"}});
                setTimeout(function(){
                    new Effect.Opacity("loginError", {from:1, to:0})}, 10000);
            }
        });
    }
}
Event.observe(window, "load", AppMain.Login);