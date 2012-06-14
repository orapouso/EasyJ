var Shortcut = Class.create(Widget, {
    getTitle: function() {
        return this.options.title;
    },
    updateWindowClose: function() {
        this.kill();
    }
});

Shortcut.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    behave: function($super) {
        $super();
        this.createTitle();
    },
    after: function() {
        this.elements.container.observe(this.widget.options.executeOnDblClick ? "dblclick" : "click", this.widget.execute.bind(this.widget));
        this.updateTitle();
    },
    createTitle: function() {
        this.elements.container.insert(
            new Element("div", {"class":"title"}).insert(
                this.elements.title = new Element("span")
            )
        );
    },
    updateTitle: function() {
        this.widget.elements.title.update(this.widget.options.title);
    },
    setIcon: function() {
        this.elements.iconImg.src = this.widget.options.icon.interpolate({theme:this.widget.getParent().getTheme() || "."});
    }
});

Shortcut.DesktopRenderer = Class.create(Shortcut.BasicRenderer, {
    behave: function($super) {
        $super();
        this.createIcon();
    },
    after: function($super) {
        $super();
        this.elements.container
            .addClassName("fixed")
            .observe("mousedown", this.widget.focus.bindAsEventListener(this.widget))
            .observe("mousedown", this.widget.drag.bindAsEventListener(this.widget));
    },
    createIcon: function() {
        this.elements.container.insert(
            {top:this.elements.icon = new Element("div", {"class":"icon"}).insert(
                this.elements.iconImg = new Element("img")
            )}
        );
    }
});

Shortcut.BasicFocuser = Class.create(Widget.Behavior, {
    after: function(ev) {
        ev.stop();
    },
    behave: function(ev) {
        this.widget.elements.container
            .setStyle({zIndex:++Shortcut.zIndex})
            .addClassName("focus");
    }
});

Shortcut.BasicBlurer = Class.create(Widget.Behavior, {
    behave: function() {
        this.widget.elements.container.removeClassName("focus");
    }
});

Shortcut.BasicExecuter = Class.create(Widget.Behavior, {
    winClass: null,
    before: function() {
        this.winClass = Window;
    },
    behave: function() {
        if(!this.widget.options.singleInstance || !this.widget.executing) {
            this.widget.executing = this.execute();
        }
    },
    after: function() {
        this.widget.executing.focus();
        this.widget.executing.attachCloseObserver(this.widget.updateWindowClose.bind(this.widget));
    },
    execute: function() {
        return new this.winClass({
            content:this.widget.options.target,
            title:this.widget.getTitle(),
            bounds:this.widget.elements.container.up()
        });
    },
    destroy: function() {
        this.widget.executing = null;
        delete this.widget.executing;
        this.winClass = null;
        delete this.winClass;
    }
});

Shortcut.DesktopDragger = Class.create(Widget.Behavior.Dragger.GhostDragger, {
    startDrag: function($super) {
        if(!this.dragging) {
            $super();
            this.element.removeClassName("fixed");
        }
    },
    endDrag: function($super) {
        var container = this.widget.elements.container;
        if(this.dragging && container.hasClassName("fixed")) {
            container.insert({before:new Element("div", {"class":container.className + " clone fixed"}).clonePosition(container)});
            container.removeClassName("fixed");
        }
        $super();
    }
});

Shortcut.DesktopExecuter = Class.create(Shortcut.BasicExecuter, {
    before: function() {
        this.winClass = Window.Desktop;
    },
    after: function($super) {
        this.widget.parent.addWidget(this.widget.executing);
        $super();
    }
});

Shortcut.DesktopTaskbarExecuter = Class.create(Shortcut.DesktopExecuter, {
    before: function() {
        var target = this.widget.options.target;
        this.winClass = Window.Desktop.Taskbar;
        if(Object.isString(target) && target.indexOf("http") > -1) {
            this.winClass = Window.Desktop.Taskbar.Iframe;
        }
    },
    after: function($super) {
        if(this.widget.options.taskbar) {
            var taskbar = new Container.Taskbar.Process()
                    .addWidget(new Label(this.widget.getTitle()));

            var window = this.widget.executing;

            window.attachFocusObserver(taskbar.updateWindowFocus.bind(taskbar));
            window.attachBlurObserver(taskbar.updateWindowBlur.bind(taskbar));
            window.attachCloseObserver({before:taskbar.updateWindowClose.bind(taskbar)});

            taskbar.attachFocusObserver(window.updateTaskbarFocus.bind(window))
            taskbar.attachFocusObserver({before:this.widget.parent.updateWidgetFocus.bind(this.widget.parent)});

            this.widget.options.taskbar.addWidget(taskbar);
        }
        $super();
    }
});

Shortcut.BasicKiller = Class.create(Widget.Behavior, {
    behave: function() {
        this.widget.executing = null;
    }
});

Object.extend(Shortcut, {
    id:"shortcut",
    count:0,
    zIndex:100,
    options: {
        behaviors: {
            render: Shortcut.BasicRenderer,
            execute: Shortcut.BasicExecuter,
            kill: Shortcut.BasicKiller
         },
        className:"sc-basic sc-default",
        draggable:true,
        executeOnDblClick:true,
        singleInstance:false,
        title:"New Shortcut #{count}",
        icon:"static/js/widgets/themes/#{theme}/shortcut/default_icon.png"
    }
});

Object.extend(Shortcut.Desktop = Class.create(Shortcut, {
    updateAddWidget: function() {
        this.behaviors.render.setIcon();
    },
    updateDesktopFocus: function() {
        this.blur();
    }
}), {
    options: {
        behaviors: {
            render: Shortcut.DesktopRenderer,
            execute: Shortcut.DesktopExecuter,
            drag: Shortcut.DesktopDragger,
            focus: Shortcut.BasicFocuser,
            blur: Shortcut.BasicBlurer
        }
    }
});

Object.extend(Shortcut.Desktop.Taskbar = Class.create(Shortcut.Desktop, {}), {
    options: {
        behaviors: {
            execute:Shortcut.DesktopTaskbarExecuter
        },
        taskbar: new Widget()
    }
});

Object.extend(Shortcut.Monitoracao = Class.create(Shortcut.Desktop.Taskbar, {}), {
    options: {
        icon:"static/images/monitor_icon.png",
        className:"sc-basic sc-monitor"
    }
});

Object.extend(Shortcut.CCD = Class.create(Shortcut.Desktop.Taskbar, {}), {
    options: {
        icon:"static/images/ccd_icon.png",
        className:"sc-basic sc-ccd"
    }
});

Object.extend(Shortcut.Contribuicao = Class.create(Shortcut.Desktop.Taskbar, {}), {
    options: {
        icon:"static/images/contribuicao_icon.png",
        className:"sc-basic sc-contribuicao"
    }
});