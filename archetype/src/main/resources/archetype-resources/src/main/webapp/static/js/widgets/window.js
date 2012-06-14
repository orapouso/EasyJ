var Window = Class.create(Widget, {
    setPosDim: function(posDim) {
        posDim = posDim || {};
        this.behaviors.resize.setPosDim(posDim);
    },
    setPosition: function() {
        var optPos = this.options.position;
        var center = -1;
        var pos = {};

        if(typeof optPos == "string") {
            center = optPos.split("-").invoke("indexOf", "center");
            if(center[0] > -1) {
                pos.marginTop = -1 * parseInt(this.getCorrectedHeight() / 2);
            }
            if(center[1] > -1) {
                pos.marginLeft = -1 * parseInt(this.getCorrectedWidth() / 2);
            }
            pos = Object.extend(pos, Window.positions[optPos] || Window.positions["top-left"]);
        } else {
            pos = optPos;
        }

        this.setPosDim(pos);

        if(typeof optPos == "string" || optPos.bottom || optPos.right) {
            var containerOffset = this.getContainer().cumulativeOffset();
            var boundScroll = "cumulativeScrollOffset";
            if(this.options.bounds.getScrollOffsets) {
                boundScroll = "getScrollOffsets";
            }
            boundScroll = this.options.bounds[boundScroll]();
            this.setPosDim({
                top: (containerOffset.top + boundScroll.top),
                left: (containerOffset.left + boundScroll.left),
                marginTop: 0,
                marginLeft: 0
            });
            var style =  this.getContainer().style;
            style.removeProperty("bottom");
            style.removeProperty("right");
        }
    },
    updateTitle: function(title) {
        if(title) {
            this.options.title = title;
        }
        this.behaviors.render.updateTitle();
    },
    updateContent: function(content) {
        if(content) {
            this.options.content = content;
        }
        this.behaviors.render.updateContent();
    },
    lockScreen: function() {
        if(this.options.lockScreen) {
            Window.screenLockers.set(this.id, this);
            Window.lockScreenRenderer.lockScreen(this.getContainer().up());
        }
    },
    unlockScreen: function() {
        Window.screenLockers.unset(this.id);
        Window.lockScreenRenderer.unlockScreen();
    },
    checkResize: function() {
        return this.options.resizable && !this.minimized && !this.maximized;
    },
    checkMaximize: function() {
        return this.options.resizable;
    },
    checkMinimize: function() {
        return this.options.resizable;
    },
    checkRestore: function() {
        return this.options.resizable;
    },
    getTitle: function() {
        return this.options.title;
    },
    toggleMaximizeState: function() {
        if(this.maximized) {
            this.restore();
        } else {
            this.maximize();
        }
    },
    getCorrectedWidth: function() {
        var width = this.getContainer().measure("width");
        var minWidth = this.options.minWidth;
        var maxWidth = this.options.bounds.getWidth();
        width = (minWidth && width < minWidth) ? minWidth : (maxWidth && width > maxWidth) ? maxWidth : width;
        return width;
    },
    getCorrectedHeight: function() {
        var height = this.getContainer().measure("height");
        var minHeight = this.options.minHeight;
        var maxHeight = this.options.bounds.getHeight();
        height = (minHeight && height < minHeight) ? minHeight : (maxHeight && height > maxHeight) ? maxHeight : height;
        return height;
    }
});

Window.BasicRenderer = Class.create(Widget.Behavior.Renderer, {
    behave: function($super) {
        $super();
        this.createHeader();
        this.createBody();
        this.createFooter();
        if(this.widget.options.resizable) {
            this.createResizes();
        }
        this.createControls();

        var bounds = $(document.body);
        if(this.widget.options.bounds.insert) {
            bounds = this.widget.options.bounds;
        }
        bounds.insert(this.elements.container);
    },
    after: function() {
        if(this.widget.options.maximizable && this.widget.options.resizable) {
            this.elements.header.observe("dblclick", this.widget.toggleMaximizeState.bind(this.widget));
        }

        this.updateTitle();
        this.updateContent();

        this.widget.lockScreen();
        this.widget.setPosition();
        this.widget.open();
    },
    createContainer: function($super){
        $super();
        this.elements.window = new Element("div", {"class":"wd-window"});
        this.elements.container
            .setOpacity(0)
            .insert(this.elements.window)
            .observe("mousedown", this.widget.focus.bind(this.widget));
    },
    createHeader: function(){
        this.elements.window.insert(
            this.elements.header = new Element("div", {"class":"wd-header"}).observe("mousedown", this.widget.drag.bindAsEventListener(this.widget)).insert(
                new Element("div", {"class":"wd-lef"}).insert(
                    new Element("div", {"class":"wd-rig"}).insert(
                        new Element("div", {"class":"wd-cen"}).insert(
                            this.elements.title = new Element("div", {"class":"wd-title"})
                        )
                    )
                )
            )
        );
    },
    createBody: function(){
        this.elements.window.insert(
            this.elements.body = new Element("div", {"class":"wd-body"}).insert(
                this.elements.cover = new Element("div", {"class":"wd-cover"}).observe("mousedown", this.widget.focus.bind(this.widget))
            ).insert(
                new Element("div", {"class":"wd-lef"}).insert(
                    new Element("div", {"class":"wd-rig"}).insert(
                        new Element("div", {"class":"wd-cen"}).insert(
                            this.elements.content = new Element("div", {"class":"wd-content"})
                        )
                    )
                )
            )
        );
    },
    createFooter: function(){
        this.elements.window.insert(
            this.elements.footer = new Element("div", {"class":"wd-footer"}).insert(
                new Element("div", {"class":"wd-lef"}).insert(
                    new Element("div", {"class":"wd-rig"}).insert(
                        this.elements.footerContent = new Element("div", {"class":"wd-cen"})
                    )
                )
            )
        );
    },
    createResizes: function(){
        this.elements.container.insert(
            this.elements.NHandler = new Element("div", {"class":"wd-resize wd-n wd-hori"})
        ).insert(
            this.elements.SHandler = new Element("div", {"class":"wd-resize wd-s wd-hori"})
        ).insert(
            this.elements.WHandler = new Element("div", {"class":"wd-resize wd-w wd-vert"})
        ).insert(
            this.elements.EHandler = new Element("div", {"class":"wd-resize wd-e wd-vert"})
        ).insert(
            this.elements.NWHandler = new Element("div", {"class":"wd-resize wd-n wd-w wd-diag"})
        ).insert(
            this.elements.NEHandler = new Element("div", {"class":"wd-resize wd-n wd-e wd-diag"})
        ).insert(
            this.elements.SWHandler = new Element("div", {"class":"wd-resize wd-s wd-w wd-diag"})
        ).insert(
            this.elements.SEHandler = new Element("div", {"class":"wd-resize wd-s wd-e wd-diag"})
        ).select(".wd-resize").invoke("observe", "mousedown", this.widget.resize.bindAsEventListener(this.widget));
    },
    createControls: function(){
        var controls = this.elements.controls = new Element("div", {"class":"wd-controls"});
        var optCtrls = this.widget.options;

        if(optCtrls.pinnable) {
            controls.insert(this.elements.controls.pin = new Element("div", {"class":"wd-pin"}).observe("click", this.widget.pin.bind(this.widget)));
            controls.insert(this.elements.controls.unpin = new Element("div", {"class":"wd-unpin"}).hide().observe("click", this.widget.unpin.bind(this.widget)));
        }
        if(optCtrls.minimizable && this.widget.options.resizable) {
            controls.insert(this.elements.controls.minimize = new Element("div", {"class":"wd-min"}).observe("click", this.widget.minimize.bind(this.widget)));
        }
        if((optCtrls.minimizable || optCtrls.maximizable) && this.widget.options.resizable) {
            controls.insert(this.elements.controls.restore = new Element("div", {"class":"wd-restore"}).hide().observe("click", this.widget.restore.bind(this.widget)));
        }
        if(optCtrls.maximizable && this.widget.options.resizable) {
            controls.insert(this.elements.controls.maximize = new Element("div", {"class":"wd-max"}).observe("click", this.widget.maximize.bind(this.widget)));
        }
        if(optCtrls.closable) {
            controls.insert(this.elements.controls.close = new Element("div", {"class":"wd-close"}).observe("click", this.widget.close.bind(this.widget)));
        }

        this.elements.container.insert(controls);
    },
    setSize: function() {
        var h = this.elements.container.getHeight();
        var headerH = this.elements.header.getHeight();
        var footerH = this.elements.footer.getHeight();

        h = h - headerH - footerH;
        if(h < 0) {
            h = 0;
        }
        this.elements.content.setStyle({
            height:h + "px"
        });
    },
    updateTitle: function() {
        this.elements.title.update(this.widget.options.title);
    },
    updateContent: function() {
        this.elements.content.update(this.widget.options.content || "");
    }
});

Window.IFrameRenderer = Class.create(Window.BasicRenderer, {
    behave: function($super) {
        $super();
        this.createIFrame()
    },
    createIFrame: function() {
        this.elements.content.insert(
            this.iframe = new Element("iframe", {
                "frameborder":"0",
                "scrolling":"auto",
                "width":"100%",
                "height":"100%",
                "marginheight":"0",
                "marginwidth":"0"
            })
        );
    },
    updateContent: function() {
        this.iframe.src = this.widget.options.content;
    }
});

Window.DialogRenderer = Class.create(Window.BasicRenderer, {
    createFooter: function($super) {
        $super();
        this.elements.footerContent.insert(
            this.elements.buttons = new Element("div", {"class":"wd-dialog-buttons"}).hide()
        );
        this.createButtons();
    },
    createButtons: function() {
        var buttons = this.widget.options.buttons;
        var btsEl = this.elements.buttons;
        var attr, btAttrs, observers, observer, btObs, input;
        if(buttons.size() > 0) {
            for(var i = 0; i < buttons.size(); i++) {
                btAttrs = {"type":"button"};
                observers = {};
                for(attr in buttons[i]) {
                    if(attr.substring(0, 2) == "on") {
                        observers[attr.substring(2)] = buttons[i][attr];
                    } else {
                        btAttrs[attr] = buttons[i][attr];
                    }
                }
                input = new Element("input", btAttrs);
                for(btObs in observers) {
                    observer = observers[btObs];
                    if(typeof observer == "string") {
                        observer = eval(observer);
                        if(Object.isUndefined(observer)) {
                            observer = this.widget[observers[btObs]];
                        }
                    }
                    if(Object.isFunction(observer)) {
                        input.observe(btObs, observer.bind(this.widget));
                    }
                }
                btsEl.insert(input);
            }
            btsEl.show();
        }

    }
});

Window.PromptRenderer = Class.create(Window.DialogRenderer, {
    updateContent: function($super) {
        $super();
        this.elements.content.insert(
            new Element("div", {"class":"wd-prompt"}).insert(
                this.elements.prompt = new Element("input", {"type":"text", "class":"wd-prompt-input"})
            )
        );
    }
});


Window.BasicFocuser = Class.create(Widget.Behavior, {
    behave: function() {
        this.widget.elements.container.setStyle({zIndex:Window.zIndex++});

        this.widget.elements.header.setOpacity(1);
        this.widget.elements.controls.setOpacity(1);
        this.widget.elements.cover.hide();
    }
});

Window.BasicBlurer = Class.create(Widget.Behavior, {
    behave: function() {
        this.widget.elements.header.setOpacity(this.widget.options.blurOpacity);
        this.widget.elements.controls.setOpacity(this.widget.options.blurOpacity);
        this.widget.elements.cover.show();
    }
});

Window.BasicOpener = Class.create(Widget.Behavior, {
    behave: function() {
        this.widget.elements.container.show().setOpacity(1);
    },
    after: function() {
        this.widget.focus();
    }
});

Window.BasicCloser = Class.create(Widget.Behavior, {
    after: function() {
        this.widget.unlockScreen(this);
        this.widget.destroy();
    },
    behave: function() {
        this.widget.elements.container.hide().setOpacity(0);
    }
});

Window.BasicResizer = Class.create(Widget.Behavior, {
    initialize: function($super, wb) {
        $super(wb);
        this.startResizeBind = function(ev) {
            this.startResize();
            this.resize(ev)
        }.bindAsEventListener(this);
        this.endResizeBind = this.endResize.bind(this);
    },
    before: function() {
        this.setInitialPosDim();
    },
    behave: function(ev) {
        Widget.disableTextHighlight();
        this.pointer = ev.pointer();

        this.discoverHandle(ev.element());

        document.observe("mousemove", this.startResizeBind);
        document.observe("mouseup", this.endResizeBind);
    },
    startResize: function() {
        if(!this.resizing) {
            this.resizing = true;
            this.widget.elements.cover.show();
        }
    },
    endResize: function() {
        document.stopObserving("mousemove", this.startResizeBind);
        document.stopObserving("mouseup", this.endResizeBind);

        this.resizing = false;
        this.widget.elements.cover.hide();
        Widget.enableTextHighlight();
    },
    resize: function(ev) {
        var deltaPointer = {
            x:ev.pointer().x - this.pointer.x,
            y:ev.pointer().y - this.pointer.y
        };

        deltaPointer = Object.extend(deltaPointer, {
            width:deltaPointer.x,
            left:deltaPointer.x,
            height:deltaPointer.y,
            top:deltaPointer.y
        });

        var posDim = Object.clone(this.widget.initialPosDim);

        for(var p in posDim) {
            posDim[p] = posDim[p] + (deltaPointer[p] * this.posDimMult[p]);
        }
        this.resizeTo(posDim);
    },
    resizeBy: function(posDim) {
        var dims = this.widget.elements.container.getDimensions();
        var offset = this.widget.elements.container.cumulativeScrollOffset();
        posDim.width += dims.width;
        posDim.height += dims.height;
        posDim.left += offset.left;
        posDim.top += offset.top;
        this.resizeTo(posDim);
    },
    resizeTo: function(posDim) {
        if(this.widget.options.resizable) {
            this.setPosDim(posDim);
        }
    },
    setPosDim: function(posDim) {
        posDim = this.correctBounds(posDim);

        for(var p in posDim) {
            posDim[p] = posDim[p] + "";
            if(posDim[p].indexOf("px") == -1 && posDim[p].indexOf("%") == -1) {
                posDim[p] = posDim[p] + "px";
            }
        }
        this.widget.elements.container.setStyle(posDim);
        this.widget.behaviors.render.setSize();
        this.widget.notifyResize();
    },
    setInitialPosDim: function() {
        var dims = this.widget.elements.container.getDimensions();
        var offsets = this.widget.elements.container.cumulativeOffset();
        this.widget.initialPosDim = {
            width: dims.width,
            height: dims.height,
            top: offsets.top,
            left: offsets.left
        };
    },
    discoverHandle: function(handle) {
        this.posDimMult = {width:0, height:0, left:0, top:0};

        var handleClassName = handle.className;
        if(handleClassName.indexOf("wd-w") > -1) {
            this.posDimMult.width = -1;
            this.posDimMult.left = 1;
        } else if(handleClassName.indexOf("wd-e") > -1) {
            this.posDimMult.width = 1;
        }
        if(handleClassName.indexOf("wd-n") > -1) {
            this.posDimMult.height = -1;
            this.posDimMult.top = 1;
        } else if(handleClassName.indexOf("wd-s") > -1){
            this.posDimMult.height = 1;
        }
    },
    correctBounds: function(posDim) {
        if(Object.isUndefined(posDim.width)) {
            posDim.width = this.widget.getCorrectedWidth();
        }
        if(Object.isUndefined(posDim.height)) {
            posDim.height = this.widget.getCorrectedHeight();
        }

        var minWidth = this.widget.options.minWidth;
        var minHeight = this.widget.options.minHeight;
        var maxWidth = this.widget.options.bounds.getWidth();
        var maxHeight = this.widget.options.bounds.getHeight();

        var outboundsWidth = posDim.width < minWidth || posDim.width > maxWidth;
        if(outboundsWidth && posDim.left > 0) {
            delete posDim.left;
            delete posDim.width;
        } else if(posDim.left < 0) {
            posDim.left = 0;
            delete posDim.width;
        }

        var outboundsHeight = posDim.height < minHeight || posDim.height > maxHeight;
        if(outboundsHeight && posDim.top > 0) {
            delete posDim.top;
            delete posDim.height;
        } else if(posDim.top < 0) {
            posDim.top = 0;
            delete posDim.height;
        }

        return posDim;
    },
    disable: function() {
        this.widget.options.bkpResizable = this.widget.options.resizable;
        this.widget.options.resizable = false;
    },
    enable: function() {
        this.widget.options.resizable = this.widget.options.bkpResizable;
    }
});

Window.BasicDragger = Class.create(Widget.Behavior.Dragger, {
    startDrag: function($super) {
        if(!this.dragging) {
            $super();
            this.widget.elements.cover.show();
        }
    },
    endDrag: function($super) {
        this.widget.elements.cover.hide();
        $super();
    }
});

Window.BasicMaximizer = Class.create(Widget.Behavior, {
    before: function() {
        this.widget.behaviors.resize.setInitialPosDim();
        if(!this.widget.restorePosDim) {
            this.widget.restorePosDim = this.widget.initialPosDim;
        }
    },
    after: function() {
        this.hide();
        this.widget.behaviors.restore.show();
        this.widget.maximized = true;
    },
    behave: function() {
        this.widget.behaviors.resize.resizeTo({
            width:this.widget.options.bounds.getWidth(),
            height:this.widget.options.bounds.getHeight(),
            top:0,
            left:0
        });
    },
    disable: function() {
        var rendMax = this.widget.elements.controls.maximize;
        if(rendMax) {
            rendMax.addClassName("disabled");
        }
    },
    enable: function() {
        var rendMax = this.widget.elements.controls.maximize;
        if(rendMax) {
            rendMax.removeClassName("disabled");
        }
    },
    hide: function() {
        var rendMax = this.widget.elements.controls.maximize;
        if(rendMax) {
            rendMax.hide();
        }
    },
    show: function() {
        var rendMax = this.widget.elements.controls.maximize;
        if(rendMax) {
            rendMax.show();
        }
    }
});

Window.BasicMinimizer = Class.create(Widget.Behavior, {
    before: function() {
        this.widget.behaviors.resize.setInitialPosDim();
    },
    after: function(){
        this.widget.minimized = true;
        this.widget.unlockScreen();
    },
    behave: function() {
        this.widget.behaviors.resize.resizeTo({
            width:this.widget.options.minWidth,
            height:this.widget.options.minHeight,
            top:0,
            left:0
        });

        this.hide();
        this.widget.behaviors.maximize.hide();
        this.widget.behaviors.restore.show();
    },
    disable: function() {
        var rendMin = this.widget.elements.controls.minimize;
        if(rendMin) {
            rendMin.addClassName("disabled");
        }
    },
    enable: function() {
        var rendMin = this.widget.elements.controls.minimize;
        if(rendMin) {
            rendMin.removeClassName("disabled");
        }
    },
    hide: function() {
        var rendMin = this.widget.elements.controls.minimize;
        if(rendMin) {
            rendMin.hide();
        }
    },
    show: function() {
        var rendMin = this.widget.elements.controls.minimize;
        if(rendMin) {
            rendMin.show();
        }
    }
});

Window.TaskbarMinimizer = Class.create(Window.BasicMinimizer, {
    after: function($super) {
        $super();
        this.widget.blur();
    },
    behave: function($super) {
        if(this.widget.options.taskbar) {
            this.widget.behaviors.resize.resizeTo({
                width:0,
                height:0,
                top:0,
                left:0
            });
        } else {
            $super();
        }
    }
});


Window.BasicRestorer = Class.create(Widget.Behavior, {
    after: function() {
        if(this.widget.maximized && this.widget.minimized) {
            this.widget.initialPosDim = this.widget.restorePosDim;
        } else {
            this.hide();
            this.widget.restorePosDim = null;
            this.widget.behaviors.maximize.show();
            this.widget.maximized = false;
        }

        this.widget.lockScreen();

        this.widget.behaviors.minimize.show();
        this.widget.minimized = false;
    },
    behave: function() {
        this.widget.behaviors.resize.resizeTo(Object.clone(this.widget.initialPosDim));
    },
    disable: function() {
        var rendRestore = this.widget.elements.controls.restore;
        if(rendRestore) {
            rendRestore.addClassName("disabled");
        }
    },
    enable: function() {
        var rendRestore = this.widget.elements.controls.restore;
        if(rendRestore) {
            rendRestore.removeClassName("disabled");
        }
    },
    hide: function() {
        var rendRestore = this.widget.elements.controls.restore;
        if(rendRestore) {
            rendRestore.hide();
        }
    },
    show: function() {
        var rendRestore = this.widget.elements.controls.restore;
        if(rendRestore) {
            rendRestore.show();
        }
    }
});

Window.BasicPinner = Class.create(Widget.Behavior, {
    after: function() {
        this.hide();
        this.widget.behaviors.unpin.show();
    },
    behave: function() {
        var behaviors = this.widget.behaviors;
        behaviors.resize.disable();
        behaviors.drag.disable();
        behaviors.maximize.disable();
        behaviors.minimize.disable();
        behaviors.restore.disable();
    },
    hide: function() {
        var rendPin = this.widget.elements.controls.pin;
        if(rendPin) {
            rendPin.hide();
        }
    },
    show: function() {
        var rendPin = this.widget.elements.controls.pin;
        if(rendPin) {
            rendPin.show();
        }
    }
});

Window.BasicUnpinner = Class.create(Widget.Behavior, {
    after: function() {
        this.hide();
        this.widget.behaviors.pin.show();
    },
    behave: function() {
        var behaviors = this.widget.behaviors;
        behaviors.resize.enable();
        behaviors.drag.enable();
        behaviors.maximize.enable();
        behaviors.minimize.enable();
        behaviors.restore.enable();
    },
    hide: function() {
        var rendUnpin = this.widget.elements.controls.unpin;
        if(rendUnpin) {
            rendUnpin.hide();
        }
    },
    show: function() {
        var rendUnpin = this.widget.elements.controls.unpin;
        if(rendUnpin) {
            rendUnpin.show();
        }
    }
});

Window.BasicLockScreenRenderer = {
    lockScreen: function(container) {
        if(!Window.lockScreen) {
            Window.lockScreen = new Element("div", {"id":"wd-lockScreen", "class":"wd-lockScreen"}).setOpacity(Window.lockScreenOpacity || 0.5)
                .setStyle({
                    "width":document.viewport.getWidth(),
                    "display":"none",
                    "zIndex":Window.zIndex-1
                });
            $(container || document.body).insert(Window.lockScreen);
        }
        Window.lockScreen.show();
    },
    unlockScreen: function() {
        if(Window.screenLockers.size() == 0 && Window.lockScreen) {
            Window.lockScreen.hide();
        }
    }
};

Object.extend(Window, {
    id:"window",
    count:0,
    zIndex:1000,
    screenLockers:$H(),
    lockScreenRenderer:Window.BasicLockScreenRenderer,
    lockScreenOpacity:0.5,
    positions: {
        "top-left"      : {top:"0px",    left:"0px"},
        "top-right"     : {top:"0px",    right:"0px"},
        "top-center"    : {top:"0px",    left:"50%"},
        "bottom-left"   : {bottom:"0px", left:"0px"},
        "bottom-right"  : {bottom:"0px", right:"0px"},
        "bottom-center" : {bottom:"0px", left:"50%"},
        "center-left"   : {top:"50%",    left:"0px"},
        "center-right"  : {top:"50%",    right:"0px"},
        "center-center" : {top:"50%",    left:"50%"}
    },
    options: {
        behaviors: {
            render: Window.BasicRenderer,
            open: Window.BasicOpener,
            close: Window.BasicCloser,
            resize: Window.BasicResizer,
            drag: Window.BasicDragger,
            maximize: Window.BasicMaximizer,
            minimize: Window.BasicMinimizer,
            restore: Window.BasicRestorer,
            pin: Window.BasicPinner,
            unpin: Window.BasicUnpinner,
            focus: Window.BasicFocuser,
            blur: Window.BasicBlurer
        },
        className:"wd-basic",
        position:"center-center",
        resizable:true,
        draggable:true,
        lockScreen:false,
        top:0,
        left:0,
        pinnable:true,
        maximizable:true,
        minimizable:true,
        closable:true,
        bounds:document.viewport,
        minWidth:0,
        minHeight:0,
        title:"New Window #{count}",
        blurOpacity:0.4
    }
});

Window.Desktop = Class.create(Window, {
    updateTaskbarFocus: function() {
        if(this.minimized) {
            this.restore();
        }
        this.behaviors.focus.start();
    },
    updateDesktopFocus: function() {
        this.blur();
    }
});

Object.extend(Window.Desktop.Taskbar = Class.create(Window.Desktop, {
}), {
    options:{
        behaviors: {
            minimize:Window.TaskbarMinimizer
        }
    }
});

Object.extend(Window.Desktop.Taskbar.Iframe = Class.create(Window.Desktop.Taskbar, {
}), {
    options:{
        behaviors: {
            render:Window.IFrameRenderer
        }
    }
});

Object.extend(Window.Dialog = Class.create(Window, {
    initialize: function($super, content, options) {
        this.content = content;
        $super(options);
    },
    setUp: function($super, options) {
        $super(options);
        this.options.content = this.content;
        this.content = null;
        delete this.content;
    }
}), {
    options:{
        behaviors: {
            render:Window.BasicRenderer
        },
        title:"Dialog #{count}",
        pinnable:false,
        maximizable:false,
        minimizable:false
    }
});
function dialogbox(content, options) {
    return new Window.Dialog(content, options);
}

Object.extend(Window.Alert = Class.create(Window.Dialog, {
    setUp: function($super, options) {
        $super(options);
        this.options.buttons = [{"value":"OK", "onclick":this.close}];
    }
}), {
    options:{
        behaviors: {
            render:Window.DialogRenderer
        },
        title:"Alert #{count}",
        lockScreen: true
    }
});
function alertbox(content, options) {
    return new Window.Alert(content, options);
}

Object.extend(Window.Confirm = Class.create(Window.Dialog, {
    setUp: function($super, options) {
        $super(options);
        this.options.buttons = [{"value":"Cancel", "onclick":this.answer.bind(this, false)}, {"value":"OK", "onclick":this.answer.bind(this, true)}];
    },
    answer: function(answer) {
        this.options.answer(answer);
        this.close();
    }
}), {
    options:{
        answer:Prototype.K,
        behaviors: {
            render:Window.DialogRenderer
        },
        title:"Confirm #{count}"
    }
});
function confirmbox(content, options) {
    return new Window.Confirm(content, options);
}

Object.extend(Window.Prompt = Class.create(Window.Alert, {
    setUp: function($super, options) {
        $super(options);
        this.options.buttons = [{"value":"Cancel", "onclick":this.close.bind(this)}, {"value":"OK", "onclick":this.answer.bind(this)}];
    },
    answer: function() {
        var answer = this.elements.prompt.value;
        this.options.answer(answer);
        this.close();
    }
}), {
    options:{
        answer:Prototype.K,
        behaviors: {
            render:Window.PromptRenderer
        },
        title:"Prompt #{count}"
    }
});
function promptbox(content, options) {
    return new Window.Prompt(content, options);
}