var Select = Class.create(Widget, {
    items:[],
    selected:null,
    selectedIndex:-1,
    setUp: function($super, options) {
        $super(options);
        var unselected = this.options.unselected ? [Object.extend({text:"Select", value:""}, this.options.unselected || {})] : [];
        this.items = unselected.concat(this.options.items || []);
        if(this.blur) {
            this.blurBind = function(){this.blur()}.bind(this);
            this.attachSelectObserver(this.blurBind);
        }
    },
    addItem: function(item, i) {
        if(Object.isUndefined(i)) {
            i = this.items.length;
        }
        this.items[i] = item;
        this.behaviors.render.renderItem(item);
        return this;
    },
    removeItem: function(i) {
        var item = this.items[i];
        delete this.items[i];
        item.element.purge();
        item.element.remove();

        for(var l = this.items.length; i < l - 1; i++) {
            this.items[i] = this.items[i+1];
        }
        this.items.length--;
        return this;
    },
    showItems: function() {
        if(this.behaviors.render.showItems) {
            this.behaviors.render.showItems();
        }
    },
    reset: function() {
        var item = this.items.find(function(item){
            return item.selected;
        }) || this.items[0];
        this.select(item.value);
    },
    observe: function(eventName, handler) {
        switch(eventName) {
            case "change":
            case "deselect":
                this["notifyCustom" + eventName.capitalize()] = function() {
                    handler(this);
                }
                break;
            default:
                this.getContainer().observe(eventName, handler);
        }
        return this;
    }
});

Select.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    behave: function($super) {
        $super();
        var container = this.getContentContainer();
        container.observe("click", this.widget.focus.bind(this.widget)).insert(
            this.elements.hidden = new Element("input", {"type":"hidden", "name":this.widget.options.name})
        ).insert(
            this.elements.selected = new Element("div" ,{"class":"sl-selected"})
        ).insert(
            new Element("div", {"class":"sl-arrow-container"}).insert(
                new Element("div", {"class":"sl-arrow"})
            )
        ).insert(
            this.elements.options = new Element("div", {"class":"sl-options-container"}).hide().insert(
                this.elements.inner = new Element("div", {"class":"sl-options-inner"})
            )
        );

        var items = this.widget.items;
        items.each(this.renderItem, this);
        this.resizeOptionsContainer();

        this.widget.selected = this.widget.options.unselected;
        this.widget.selectedEl = this.elements.inner.firstDescendant();
        this.updateSelect();
    },
    after: function() {
        this.widget.attachSelectObserver(this.updateSelect.bind(this));
        this.widget.attachFocusObserver(this.showItems.bind(this));
        this.widget.attachBlurObserver(this.hideItems.bind(this));
    },
    renderItem: function(item) {
        this.elements.inner.insert(
            item.element = new Element("div", {"class":"sl-item"})
                .store("item", item)
                .update(item.text)
                .observe("click", this.widget.select.bindAsEventListener(this.widget))
        );
    },
    showItems: function() {
        this.elements.options.show();
        this.resizeOptionsContainer();
    },
    hideItems: function() {
        this.elements.options.hide();
    },
    resizeOptionsContainer: function() {
        if(!this.itemHeight) {
            this.itemHeight = this.elements.inner.firstDescendant().getHeight();
            this.maxOptionsHeight = this.widget.options.size * this.itemHeight;
        }
        this.elements.options.setStyle({
            height:Math.min(this.maxOptionsHeight, this.widget.items.length * this.itemHeight) + "px"
        });
    },
    updateSelect: function() {
        var selected = this.widget.selected;
        this.elements.hidden.value = selected.value || "";
        this.elements.selected.update(this.widget.selectedEl.innerHTML);
        this.hideItems();
        if(this.widget.notifyCustomChange) {
            this.widget.notifyCustomChange();
        }
    }
});

Select.BasicSelecter = Class.create(Widget.Behavior, {
    behave: function(ev) {
        if(ev.findElement) {
            this.selectElement(ev.findElement());
            ev.stop();
        } else {
            this.selectValue(ev);
        }
    },
    selectValue: function(value) {
        var item = this.widget.items.find(function(item){
            return item.value == value;
        });
        if(item) {
            this.selectElement(item.element);
        }
    },
    selectElement: function(el) {
        if(this.widget.notifyCustomDeselect) {
            this.widget.notifyCustomDeselect();
        }
        el.siblings().invoke("removeClassName", "sl-selected-option");
        el.addClassName("sl-selected-option");
        this.widget.selectedEl = el.clone(true);
        var item = el.retrieve("item");
        this.widget.selected = item;
        this.widget.selectedIndex = this.widget.items.indexOf(item);
    }
});

Select.BasicFocuser = Class.create(Widget.Behavior, {
    behave: function(ev) {
        Event.stopObserving(document, "click", this.widget.blurBind);
        Event.observe(document, "click", this.widget.blurBind);
        ev.stop();
    }
});

Select.BasicBlurer = Class.create(Widget.Behavior, {
    behave: function() {
        Event.stopObserving(document, "click", this.widget.blurBind);
    }
});

Object.extend(Select, {
    id:"select",
    count:0,
    options: {
        className:"sl-select",
        name:"select",
        size:5,
        behaviors: {
            render:Select.BasicRenderer,
            select: Select.BasicSelecter,
            focus: Select.BasicFocuser,
            blur: Select.BasicBlurer
        },
        unselected: null
    }
});

