var Protobox = {
    //Load adapted from Scriptaculous
    //http://script.aculo.us/
    require: function(libraryName) {
        try{
            // inserting via DOM fails in Safari 2.0, so brute force approach
            document.write('<script type="text/javascript" src="'+libraryName+'"><\/script>');
        } catch(e) {
            // for xhtml+xml served content, fall back to DOM methods
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = libraryName;
            document.getElementsByTagName('head')[0].appendChild(script);
        }
    },
    load: function(lib) {
        function convertVersionString(versionString) {
            var v = versionString.replace(/_.*|\./g, '');
            v = parseInt(v + '0'.times(4-v.length));
            return versionString.indexOf('_') > -1 ? v-1 : v;
        }

        if((typeof Prototype=='undefined') ||
            (typeof Element == 'undefined') ||
            (typeof Element.Methods=='undefined') ||
            (convertVersionString(Prototype.Version) <
                convertVersionString(lib.REQUIRED_PROTOTYPE)))
            throw("Widget requires the Prototype JavaScript framework >= " +
                lib.REQUIRED_PROTOTYPE);

        $$('head script[src]').findAll(function(s) {
            return s.src.match(lib.js);
        }).each(function(s) {
            var path = s.src.replace(lib.js, ''),
            includes = s.src.match(/\?.*load=([a-z,]*)/);
            (includes ? includes[1] : lib.includes || '').split(',').each(
                function(include) {
                    Protobox.require(path+include+'.js')
                }
            );
        });
    }
};

(function() {
    var borderRadius = {
        _all:        $w("borderRadius MozBorderRadius webkitBorderRadius"),
        topLeft:     $w("borderTopLeftRadius MozBorderRadiusTopleft webkitBorderTopLeftRadius"),
        bottomLeft:  $w("borderBottomLeftRadius MozBorderRadiusBottomleft webkitBorderBottomLeftRadius"),
        topRight:    $w("borderTopRightRadius MozBorderRadiusTopright webkitBorderTopRightRadius"),
        bottomRight: $w("borderBottomRightRadius MozBorderRadiusBottomright webkitBorderBottomRightRadius")
    };

    function setBorderRadius(element, borders) {
        var realBorders = {};
        if(Object.isString(borders)) {
            borders = {_all:borders};
        }
        for(var border in borders) {
            borderRadius[border].each(function(borderTranslate){
                realBorders[borderTranslate] = borders[border];
            });
        }
        element.setStyle(realBorders);

        return element;
    }
    
    function rotate(element, degree) {
        var width = element.measure("margin-box-width");
        element.setStyle({
            "MozTransform":"rotate(" + degree + "deg)",
            "webkitTransform":"rotate(" + degree + "deg)",
            "oTransform":"rotate(" + degree + "deg)",
            "msTransform":"rotate(" + degree + "deg)"
        });
        
        return element;
    }
    
    function disableTextSelection(element) {
        element.setStyle({
            "MozUserSelect":"none",
            "webkitUserSelect":"none",
            "userSelect":"none"
        });
        element.onselectstart = function(){return false;};
        return element;
    }
    function enableTextSelection(element) {
        element.setStyle({
            "MozUserSelect":"none",
            "webkitUserSelect":"none",
            "userSelect":"none"
        });
        element.onselectstart = null;
        return element;
    }

    Element.addMethods({
        setBorderRadius: setBorderRadius,
        disableTextSelection:disableTextSelection,
        enableTextSelection:enableTextSelection,
        rotate:rotate
    });

    if(!Hash.prototype.clear) {
        function clear() {
            this.keys().each(function(key){
                this.unset(key);
            }, this);
            return this;
        }

        Hash.addMethods({
            clear:clear
        });
    }

})();

Ajax.JSONRequest = Class.create(Ajax.Request, {
    initialize: function($super, url, options) {
        $super(url, Ajax.JSONRequest.buildOptions(options));
    }
});

Ajax.JSONRequest.buildOptions = function(options) {
    options = Object.clone(options) || {};
    if(!options.requestHeaders) {
        options.requestHeaders = {};
    }
    options.requestHeaders["Accept"] = "application/json";
    return Object.extend({
        on500: function(response) {
            throw "ERROR: " + response.responseText;
        }
    }, options);
}

Ajax.PeriodicalExecuter = Class.create(Ajax.Base, {
    RequestExecuter:Ajax.Request,
    initialize: function($super, url, options) {
        options = options || url;
        $super(options);
        this.started = false;
        this.url = url || "/";
        this.delay = (this.options.delay || 2);
        this.onComplete = this.options.onComplete;
        this.options.onComplete = this.endExecuting.bind(this);
    },
    start: function(url) {
        if(this.timeout) return;
        if(url) {
            this.url = url;
        }
        this.started = true;
        this.execute();
    },
    execute: function() {
        if(this.executing) return;
        this.timeout = null;
        this.executing = true;
        new this.RequestExecuter(this.url, this.options);
    },
    endExecuting: function() {
        this.executing = false;
        if(!this.stopFlag) {
            this.timeout = setTimeout(this.execute.bind(this), this.delay * 1000);
        } else {
            this.stopFlag = false;
        }
    },
    stop: function() {
        if(this.timeout != null) {
            clearTimeout(this.timeout);
            this.timeout = null;
        } else {
            this.stopFlag = true;
        }
        (this.onComplete || Prototype.emptyFunction).apply(this, arguments);
        this.started = false;
    }
});
Ajax.JSONPeriodicalExecuter = Class.create(Ajax.PeriodicalExecuter, {
    RequestExecuter:Ajax.JSONRequest
});

(function(){
    function indexOfIt(iterator, i, context){
        //NaN 'i' is the context, nullifies 'i' to initialize it correctly after
        if(!Object.isNumber(i)) {
            context = i;
            i = null;
        }

        i || (i = 0);
        var length = this.length;
        if (i < 0) i = length + i;
        for (; i < length; i++)
            if (iterator.call(context, this[i], i)) {
                return i;
            }
        return -1;
    }
    Array.prototype.indexOfIt = indexOfIt;
})();
