//TODO desktop pode ter observer para o resize e move das windows se tiver multiplos desktops
//TODO rever os esquema de themes para que ao trocar a classe do desktop, mudar todo o theme de todos os widgets
var Desktop = Class.create(Widget, {
    setUp: function($super, options) {
        options.parent = options.parent || document.body;
        $super(options);
    },
    addWidget: function($super, widget) {
        $super(widget);
        this.dock(widget);
        return this;

    },
    removeWidget: function($super, widget) {
        this.undock(widget);
        return $super(widget);

    },
    updateWidgetFocus: function(widget) {
        if(widget instanceof Window) {
            Desktop.addFocusStack(widget);
        }
        this.focus();
    },
    updateWindowMinimize: function(window) {
        Desktop.removeFocusStack(window);
        Desktop.focusNext();
    },
    getHighestZIndex: function() {
        var widgets = this.widgets.values();
        var zIndex = 0;
        var currentZIndex = zIndex;
        for(var i = 0; i < widgets.length; i++) {
            currentZIndex = parseInt(widgets[i].elements.container.getStyle("zIndex")) || 0;
            if(currentZIndex > zIndex) {
                zIndex = currentZIndex;
            }
        }
        return zIndex;
    },
    setTheme: function(theme) {
        if(theme) {
            this.theme = theme;
            this.behaviors.render.setTheme();
        }
    },
    getTheme: function() {
        return this.theme;
    }
});

Desktop.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    after: function() {
        this.elements.container.observe("mousedown", this.widget.focus.bindAsEventListener(this.widget));
        $(this.widget.options.parent).insert(this.elements.container);

        this.setTheme();
        this.widget.resize();
    },
    setSize: function() {
        var parent = this.widget.options.parent;
        parent = (parent == document.body) ? document.viewport : parent;
        var width = parent.getWidth();
        var height = parent.getHeight();
        this.elements.container.setStyle({
            "height":height + "px",
            "width":width + "px"
        });
    },
    setTheme: function() {
        if(this.elements.container) {
            this.elements.container.up("body").className = this.widget.theme;
        }
    }
});

Desktop.BasicDocker = Class.create(Widget.Behavior, {
    behave: function(widget) {
        if(!(widget instanceof Container)) {
            widget.attachFocusObserver({before:this.widget.updateWidgetFocus.bind(this.widget)});
            if(widget instanceof Window) {
                widget.attachMinimizeObserver(this.widget.updateWindowMinimize.bind(this.widget));
            }
            this.widget.attachFocusObserver(widget.updateDesktopFocus.bind(widget));
        }
    }
});

Desktop.BasicUndocker = Class.create(Widget.Behavior, {
    behave: function(widget) {
        this.widget.detachFocusObserver(widget);

        if(widget instanceof Window) {
            Desktop.removeFocusStack(widget);
            Desktop.focusNext();
        }
    }
});

Desktop.BasicResizer = Class.create(Widget.Behavior, {
    before: function() {
        if(!this.resizeBind) {
            this.widget.resizeBind = this.widget.resize.bind(this);
            Event.observe(window, "resize", this.widget.resizeBind);
        }
    },
    behave: function() {
        this.widget.behaviors.render.setSize();
    },
    destroy: function($super) {
        Event.stopObserving(window, "resize", this.widget.resizeBind);
        this.widget.resizeBind = null;
        delete this.widget.resizeBind;
        $super();
    }
});

Object.extend(Desktop, {
    id:"desktop",
    count:0,
    zIndex:1000,
    options: {
        behaviors: {
            render: Desktop.BasicRenderer,
            focus: Widget.Behavior,
            dock: Desktop.BasicDocker,
            undock: Desktop.BasicUndocker,
            resize: Desktop.BasicResizer
        },
        className:"ubuntu"
    },
    focusStack: [],
    addFocusStack: function(widget){
        Desktop.removeFocusStack(widget);
        Desktop.focusStack.push(widget);
    },
    removeFocusStack: function(widget){
        Desktop.focusStack = this.focusStack.without(widget);
    },
    focusNext: function() {
        if(Desktop.focusStack.length > 0) {
            Desktop.focusStack.last().focus();
        }
    }
});