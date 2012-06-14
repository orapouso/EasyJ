var Label = Class.create(Widget, {
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

Label.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    after: function() {
        this.widget.updateContent();
    },
    updateContent: function() {
        this.elements.container.update(this.widget.options.content);
    }
});

Object.extend(Label, {
    id:"label",
    count:0,
    options: {
        className:"label",
        behaviors: {
            render:Label.BasicRenderer
        },
        content:"Label #{count}"
    }
});

