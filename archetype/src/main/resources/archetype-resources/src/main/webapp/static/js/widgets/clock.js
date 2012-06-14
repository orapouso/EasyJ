var Clock = Class.create(Widget, {
    initialize: function($super, options) {
        $super(options);
        this.initClock();
    },
    initClock: function() {
        this.configInterval();
        this.today = new Date();
        this.clockIntervar = setInterval(this.clockTick.bind(this), this.interval);
        this.updateClock();
    },
    configInterval: function() {
        var format = this.options.format;
        var intervals = [1000, 60, 60, 24];
        this.interval = 1;
        if(format.indexOf("S") > -1) {
            this.interval = intervals[0];
            this.intervalUnit = "second";
        } else if(format.indexOf("M") > -1) {
            this.interval = intervals[0] * intervals[1];
            this.intervalUnit = "minute";
        } else if(["H", "I", "p"].find(function(form){return format.indexOf(form) > -1;})) {
            this.interval = intervals[0] * intervals[1] * intervals[2];
            this.intervalUnit = "hour";
        } else if(["d", "A", "a", "w", "o"].find(function(form){return format.indexOf(form) > -1;})) {
            this.interval = intervals[0] * intervals[1] * intervals[2] * intervals[3];
            this.intervalUnit = "day";
        }
    },
    clockTick: function() {
        this.today = new Date();
        this.updateClock();
    },
    updateClock: function() {
        this.elements.clock.update(this.today.strftime(this.options.format));
    },
    openCalendar: function() {
        this.elements.calendar.show();
    }
});

Clock.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    behave: function($super) {
        $super();
        this.createClock();
    },
    createClock: function() {
        this.elements.container.insert(
            this.elements.clock = new Element("div", {"class":"clock"})
        );
    }
});

Clock.CalendarRenderer = Class.create(Clock.BasicRenderer, {
    behave: function($super) {
        $super();
        this.createCalendar();
    },
    after: function() {
        this.elements.container.observe("click", this.widget.openCalendar.bind(this.widget));
    },
    createCalendar: function() {
    }
});

Object.extend(Clock, {
    id:"clock",
    count:0,
    options: {
        behaviors: {
            render: Clock.BasicRenderer
        },
        format:"%a %b %#d, %H:%M",
        className:"ck-basic"
    }
});