var Container = Class.create(Widget, {});

Container.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    after: function() {
        this.elements.container.insert(this.options.content || "");
    }
});

Container.FocusableRenderer = Class.create(Widget.Behavior.Renderer, {
    after: function() {
        this.elements.container.observe("click", this.widget.focus.bind(this.widget));
    }
});

Container.BasicFocuser = Class.create(Widget.Behavior, {
    behave: function() {
        this.widget.elements.container.addClassName("focus");
    }
});

Container.BasicBlurer = Class.create(Widget.Behavior, {
    behave: function() {
        this.widget.elements.container.removeClassName("focus");
    }
});

Container.ProcessesBarRenderer = Class.create(Widget.Behavior.Renderer, {
    after: function() {
        this.resizeBind = this.resize.bind(this);
        Event.observe(window, "resize", this.resizeBind);
    },
    setSize: function() {
        this.resize();
    },
    resize: function() {
        var widgets = this.widget.parent.widgets.values();
        var width = 0;
        for(var i = 0; i < widgets.length; i++) {
            if(widgets[i].elements.container != this.elements.container) {
                width += widgets[i].elements.container.getWidth();
            }
        }

        width = this.widget.parent.getContainer().getWidth() - width;

        this.elements.container.setStyle({
            "width":width + "px"
        });
    },
    destroy: function($super) {
        Event.stopObserving(window, "resize", this.resizeBind);
        this.resizeBind = null;
        delete this.resizeBind;
        $super();
    }
});

Object.extend(Container, {
    id:"container",
    count:0,
    options: {
        className:"ct-container"
    }
});

Object.extend(Container.Taskbar = Class.create(Container, {
    setSize: function() {
        this.getWidget("desktops").behaviors.render.setSize();
        this.getWidget("processes").behaviors.render.setSize();
    },
    updateAddWidget: function() {
        this.parent.attachDockObserver(this.updateDesktopDock.bind(this));
    },
    updateDesktopDock: function() {
        this.elements.container.setStyle({"zIndex":this.parent.getHighestZIndex() + Container.Taskbar.zIndex});
    }
}), {
    zIndex: 1000,
    options: {
        className: "ct-taskbar"
    }
});

Object.extend(Container.Taskbar.Processes = Class.create(Container, {}), {
    options: {
        behaviors: {
            render: Container.ProcessesBarRenderer
        }
    }
});

Object.extend(Container.Taskbar.Process = Class.create(Container, {
    updateWindowFocus: function() {
        this.behaviors.focus.start();
    },
    updateWindowBlur: function() {
        this.blur();
    },
    updateWindowClose: function() {
        this.destroy();
    }
}), {
    options: {
        className:"ct-window",
        behaviors: {
            render: Container.FocusableRenderer,
            focus: Container.BasicFocuser,
            blur: Container.BasicBlurer
        }
    }
});