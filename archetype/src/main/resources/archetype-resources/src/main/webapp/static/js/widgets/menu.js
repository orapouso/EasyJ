var Menu = Class.create(Widget, {
    setUp: function($super, options) {
        if(typeof options == "string") {
            options = {content:options};
        }
        $super(options);
        this.options.content = this.options.content.interpolate({count:this.count})
    },
    updateContent: function() {
        this.behaviors.render.updateContent();
    }
});

Menu.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    after: function() {
        this.elements.container.observe("click", this.widget.execute.bind(this.widget));
        this.widget.updateContent();
    },
    updateContent: function() {
        this.elements.container.update(this.widget.options.content);
    }
});

Menu.BasicExecuter = Class.create(Widget.Behavior, {
    behave: function() {
        if(this.widget.options.target && Object.isFunction(this.widget.options.target)) {
            this.widget.options.target();
        }
        if(this.widget.widgets.size() > 0) {
            this.widget.open();
        }
    }
});

Menu.BasicOpener = Class.create(Widget.Behavior, {
    behave: function() {
        if(this.widget.widgets.size() > 0) {
            this.widget.elements.children.show();
        }
    }
});

Object.extend(Menu , {
    id:"menu",
    count:0,
    options: {
        className:"menu",
        behaviors: {
            render:Menu.BasicRenderer,
            execute:Menu.BasicExecuter,
            open:Menu.BasicOpener
        },
        content:""
    }
});

