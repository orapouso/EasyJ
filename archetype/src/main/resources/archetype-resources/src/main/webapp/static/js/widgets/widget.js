var Widget = Class.create({
    id:null,
    options:null,
    observers:null,
    widgets:null,
    initialize: function(options) {
        this.setUp(options);
        this.afterSetUp();
        this.render();
    },
    setUp: function(options) {
        this.options = {};
        this.count = this.extendOptions(options);
        this.id = Widget.getClassProperty(this.constructor, "id") + "_" + (this.options.id || this.count);
        this.observers = $H();
        this.widgets = $H();
        this.theme = this.options.theme || "";
        if(this.options.title) {
            this.options.title = this.options.title.interpolate({
                count:this.count
            });
        }

        this.setBehaviors();
    },
    afterSetUp: Prototype.K,
    getWidget: function(widget) {
        var id = Widget.getClassProperty(this.constructor, "id") + "_" + (widget.id || widget);
        return Widget.getWidget(id);
    },
    extendOptions: function(options) {
        options = options || {};
        var parent = this.constructor;
        var count = null;
        do {
            options = Object.extend(Object.clone(parent.options || {}), options);
            if(options.behaviors && parent.options) {
                options.behaviors = Object.extend(Object.clone(parent.options.behaviors || {}), options.behaviors || {});
            }
            if(!count && Object.isNumber(parent.count)) {
                count = ++parent.count;
            }
        } while((parent = parent.superclass) != null)
        this.options = options;
        return count;
    },
    setBehaviors: function(behaviors) {
        behaviors = behaviors || this.options.behaviors;

        this.behaviors = {};

        Object.keys(behaviors).each(function(behavior){
            if(Object.isFunction(behaviors[behavior])) {
                this.behaviors[behavior] = new behaviors[behavior](this);
            } else {
                this.behaviors[behavior] = behaviors[behavior];
            }

            var bCap = behavior.capitalize();
            this.observers.set(behavior, {
                before:[],
                after:[]
            });
            if(Object.isUndefined(this[behavior])) {
                this[behavior] = function() {
                    if(this["check" + bCap]) {
                        var evt = (arguments.length > 0 && arguments[0] instanceof Event) ? arguments[0] : null;
                        if(this.behaviors[behavior]
                            && (  evt == null
                               || evt.findElement() == window
                               || evt.element().retrieve("widget") == this
                               || evt.element().ancestors().invoke("retrieve", "widget").compact().indexOf(this) == 0)) {
                            this["notifyBefore" + bCap].apply(this, arguments);
                            this.behaviors[behavior].start.apply(this.behaviors[behavior], arguments);
                            this["notifyAfter" + bCap].apply(this, arguments);
                        }
                    }
                    return this;
                }.bind(this);
            }
            if(Object.isUndefined(this["check" + bCap])) {
                this["check" + bCap] = function() {
                    return true;
                }.bind(this)
            }
            this["attach" + bCap + "Observer"] = function(updates) {
                if(updates.before) {
                    this.attachObserver(behavior, {
                        update:updates.before
                    }, "before");
                }
                if(updates.after || !updates.before) {
                    this.attachObserver(behavior, {
                        update:updates.after || updates
                    }, "after");
                }
            }.bind(this);
            this["detach" + bCap + "Observer"] = function(observer) {
                this.detachObserver(behavior, observer);
            }.bind(this);
            this["get" + bCap + "Observers"] = function() {
                return this.getObservers(behavior);
            }.bind(this);
            this["notifyBefore" + bCap] = function() {
                var args = [behavior, "before"];
                args = args.concat($A(arguments));
                this.notify.apply(this, args);
            }.bind(this);
            this["notifyAfter" + bCap] = function() {
                var args = [behavior, "after"];
                args.concat(arguments);
                this.notify.apply(this, args);
            }.bind(this);
            this["notify" + bCap] = this["notifyAfter" + bCap];
        }, this);
    },
    destroy: function() {
        if(this.parent) {
            this.parent.removeWidget(this)
            this.parent = null;
            delete this.parent;
        }

        Widget.clear(this.options);
        this.options = null;
        delete this.options;

        var keys = this.widgets.keys();
        for(var i = 0; i < keys.length; i++) {
            this.removeWidget(keys[i]);
        }
        this.widgets = null;
        delete this.widgets;

        for(var a in this.behaviors) {
            this.behaviors[a].destroy();
            this.behaviors[a] = null;
            delete this.behaviors[a];
        }
        this.behaviors = null;
        delete this.behaviors;

        keys = this.observers.keys();
        var observers;
        for(i = 0; i < keys.length; i++) {
            observers = this.observers.unset(keys[i]);
            observers.before.clear();
            observers.after.clear();
        }
        this.observers = null;
        delete this.observers;
    },
    addWidget: function(widget) {
        Widget.widgets.set(widget.id, widget);
        this.widgets.set(widget.id, widget);
        this.behaviors.render.addWidget(widget);
        widget.parent = this;

        widget.updateAddWidget();

        return this;
    },
    removeWidget: function(widget) {
        Widget.widgets.unset(widget.id || widget);
        this.widgets.unset(widget.id || widget);
        this.behaviors.render.removeWidget(widget);
        return this;
    },
    insert: function(element) {
        this.elements.container.insert(element);
        return this;
    },
    observe: function(eventName, handler) {
        this.elements.container.observe(eventName, handler);
        return this;
    },
    updateAddWidget: Prototype.K,
    getContainer: function() {
        return this.elements.container;
    },
    getContentContainer: function() {
        return this.behaviors.render.getContentContainer();
    },
    attachObserver: function(behavior, observer, when) {
        this.detachObserver(behavior, observer, when);

        var observers = this.getObservers(behavior)[when];
        observers.push(observer);
    },
    detachObserver: function(behavior, observer, when) {
        var i = this.indexOfObserver(behavior, observer, when);
        var observers = this.getObservers(behavior);
        if(i > -1) {
            if(!when) {
                when = "before";
                if(i > observers.before.length - 1) {
                    when = "after";
                    i -= observers.before.length;
                }
            }

            observers = observers[when];
            for(; i < observers.length - 1; i++) {
                observers[i] = observers[i+1];
            }
            observers[observers.length - 1] = null;
            observers.length--;
        }
    },
    indexOfObserver: function(behavior, observer, when) {
        var idx = -1;
        var observers = this.getObservers(behavior);
        if(!when) {
            if((idx = this.indexOfObserver(behavior, observer, "before") == -1)) {
                idx = this.indexOfObserver(behavior, observer, "after");
            }
            return idx;
        }

        observers = observers[when];
        if(observers.length > 0) {
            for(var i = 0, l = observers.length; i < l; i++) {
                if(observers[i].update == observer.update) {
                    idx = i;
                    break;
                }
            }
            if(when == "after") {
                idx += this.getObservers(behavior)["before"].length;
            }
        }
        return idx;
    },
    getObservers: function(obsType) {
        return this.observers.get(obsType);
    },
    notify: function() {
        var args = $A(arguments);
        var behavior = args.shift();
        var when = args.shift();
        if(!this.observers) return;
        var observers = this.getObservers(behavior)[when];
        if(observers) {
            args.push(this);
            observers.each(function(observer){
                observer.update.apply(observer, args);
            }, this);
        }
    },
    getTheme: function() {
        return this.theme || ((this.parent) ? this.parent.getTheme() : null);
    },
    getParent: function() {
        if(this.parent) return this.parent;
        if(!this.elements || !this.elements.container) return null;

        return this.elements.container.ancestors().find(function(el){
            return el.retrieve("widget");
        });
    }
});

Widget.Behavior = Class.create({
    initialize: function(widget) {
        this.widget = widget;
    },
    before: Prototype.K,
    behave: Prototype.K,
    after: Prototype.K,
    start: function() {
        this.before.apply(this, arguments);
        this.behave.apply(this, arguments);
        this.after.apply(this, arguments);
    },
    destroy: function() {
        this.widget = null;
        delete this.widget;
    }
});

Widget.Behavior.Renderer = Class.create(Widget.Behavior, {
    initialize: function($super, widget) {
        $super(widget);
        this.elements = this.widget.elements = {};
    },
    behave: function() {
        if(!this.elements.container) {
            this.createContainer();
        }
    },
    createContainer: function() {
        this.elements.container = new Element("div", {
            "class":this.widget.options.className || "",
            "id":this.widget.id
        }).store("widget", this.widget);
    },
    setTheme: function() {
        this.elements.container.addClassName(this.widget.theme);
    },
    addWidget: function(widget) {
        this.getContentContainer().insert(widget.elements.container);
    },
    removeWidget: Prototype.K,
    setSize:Prototype.K,
    getContentContainer: function() {
        return this.widget.getContainer();
    },
    destroy: function($super) {
        this.elements.container.purge();
        this.elements.container.remove();

        Widget.clear(this.elements);

        this.widget.elements = null;
        delete this.widget.elements;
        this.elements = null;
        delete this.elements;

        $super();
    }
});

Widget.Behavior.Dragger = Class.create(Widget.Behavior, {
    pointer:null,
    element:null,
    dragging:false,
    initialize: function($super, wb) {
        $super(wb);
        this.startDragBind = function(ev) {
            this.startDrag();
            this.drag(ev)
        }.bindAsEventListener(this);
        this.endDragBind = this.endDrag.bind(this);
    },
    before: function() {
        this.element = this.widget.elements.container;
    },
    behave: function(ev) {
        if(!this.widget.options.draggable) return;

        Widget.disableTextHighlight();

        var offsets = this.element.cumulativeOffset();
        this.mouseDelta = {
            x:ev.pointerX() - offsets.left,
            y:ev.pointerY() - offsets.top
        }
        this.scrollOffset = this.element.cumulativeScrollOffset();

        document.observe("mousemove", this.startDragBind);
        document.observe("mouseup", this.endDragBind);
    },
    startDrag: function() {
        this.dragging = true;
    },
    drag: function(ev) {
        var pos = {};
        var pointer = ev.pointer();

        if((pos.left = pointer.x - this.mouseDelta.x) - this.scrollOffset.left < 0) {
            pos.left = this.scrollOffset.left;
        }
        if((pos.top = pointer.y - this.mouseDelta.y) - this.scrollOffset.top < 0) {
            pos.top = this.scrollOffset.top;
        }

        pos.top += "px";
        pos.left += "px";
        this.element.setStyle(pos);
        this.widget.notifyDrag();
    },
    endDrag: function() {
        document.stopObserving("mousemove", this.startDragBind);
        document.stopObserving("mouseup", this.endDragBind);

        Widget.enableTextHighlight();
        this.element = null;
        this.dragging = false;
    },
    disable: function() {
        this.widget.options.bkpDraggable = this.widget.options.draggable;
        this.widget.options.draggable = false;
    },
    enable: function() {
        this.widget.options.draggable = this.widget.options.bkpDraggable;
    }
});

Widget.Behavior.Dragger.GhostDragger = Class.create(Widget.Behavior.Dragger, {
    startDrag: function($super) {
        if(!this.dragging) {
            $super();
            this.element = this.widget.elements.container.clone(true).setOpacity(0.5).addClassName("ghost").setStyle({
                "position":"absolute"
            });
            this.widget.elements.container.up().insert(this.element);
        }
    },
    endDrag: function($super) {
        if(this.element != this.widget.elements.container) {
            var pos = this.element.cumulativeOffset();
            this.widget.elements.container.setStyle({
                "top":pos.top + "px",
                "left":pos.left + "px"
                });
            this.element.remove();
        }
        $super();
    }
});

Object.extend(Widget, {
    id:"widget",
    REQUIRED_PROTOTYPE: '1.6.1',
    widgets: $H(),
    options: {
        theme:"",
        behaviors: {
            render: Widget.Behavior.Renderer
        }
    },
    js:/widget\.js(\?.*)?$/,
    includes:'clock,container,desktop,label,menu,shortcut,window',
    getWidget: function(widget) {
        return Widget.widgets.get(widget.id || widget);
    },
    clear: function() {
        var o, obj;
        for(var i = 0; i < arguments.length; i++) {
            obj = arguments[i];
            for(o in obj) {
                try {
                    obj[o] = null;
                    delete obj[o];
                } catch(e) {}
            }
        }
    },
    disableTextHighlight: function() {
        document.onselectstart = function() {
            return false;
        }
        this.onMouseDown = document.onmousedown;
        document.onmousedown = function() {
            return false;
        }
    },
    enableTextHighlight: function() {
        document.onselectstart = null;
        document.onmousedown = this.onMouseDown;
    },
    getClassProperty: function(constructor, prop) {
        if(constructor[prop]) {
            return constructor[prop];
        } else if(constructor.superclass) {
            return Widget.getClassProperty(constructor.superclass, prop);
        } else {
            return "";
        }
    },
    load: function() {
        Protobox.load(Widget);
    }
});

Widget.load();